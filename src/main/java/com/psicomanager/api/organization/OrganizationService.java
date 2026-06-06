package com.psicomanager.api.organization;

import com.psicomanager.api.organization.dto.CreateOrganizationDTO;
import com.psicomanager.api.organization.dto.MemberResponseDTO;
import com.psicomanager.api.organization.dto.OrganizationResponseDTO;
import com.psicomanager.api.organization.exception.AlreadyMemberException;
import com.psicomanager.api.organization.exception.NotMemberException;
import com.psicomanager.api.organization.exception.OrganizationNotFoundException;
import com.psicomanager.api.organization.model.MemberRole;
import com.psicomanager.api.organization.model.Organization;
import com.psicomanager.api.organization.model.OrganizationMember;
import com.psicomanager.api.user.UserRepository;
import com.psicomanager.api.user.exception.UserNotFoundException;
import com.psicomanager.api.user.model.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

/**
 * Gerencia o ciclo de vida de organizações e seus membros.
 *
 * <p>Uma organização é o tenant raiz do sistema. Todo dado (paciente, sessão,
 * plano, financeiro) pertence a uma organização.</p>
 */
@Service
@Slf4j
public class OrganizationService {

    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    private OrganizationMemberRepository memberRepo;

    @Autowired
    private UserRepository userRepo;

    // region Slug

    /**
     * Gera um slug URL-friendly a partir do nome da organização,
     * garantindo unicidade com sufixo numérico se necessário.
     *
     * @param name nome da organização
     * @return slug único no sistema
     */
    private String generateSlug(String name) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")          // remove acentos
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")      // substitui não-alfanum por hífen
                .replaceAll("^-|-$", "");            // remove hífens nas pontas

        String slug = base;
        int suffix = 2;
        while (orgRepo.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    // endregion

    // region Criação

    /**
     * Cria uma nova organização e adiciona o usuário criador como {@link MemberRole#OWNER}.
     * Define esta organização como ativa no usuário.
     *
     * @param dto  dados da nova organização
     * @param user usuário criador (será OWNER)
     * @return DTO de resposta da organização criada
     */
    @Transactional
    public OrganizationResponseDTO create(CreateOrganizationDTO dto, User user) {
        log.info("Criando organização '{}' para o usuário {}", dto.name(), user.getId());

        Organization org = new Organization();
        org.setName(dto.name());
        org.setSlug(generateSlug(dto.name()));
        orgRepo.save(org);

        OrganizationMember member = new OrganizationMember();
        member.setOrganization(org);
        member.setUser(user);
        member.setRole(MemberRole.OWNER);
        memberRepo.save(member);

        // Define como organização ativa imediatamente
        user.setActiveOrganizationId(org.getId());
        userRepo.save(user);

        log.info("Organização '{}' criada com id {} e slug '{}'", org.getName(), org.getId(), org.getSlug());
        return toDto(org, MemberRole.OWNER);
    }

    // endregion

    // region Ingresso via convite

    /**
     * Adiciona o usuário a uma organização existente via slug (código de convite).
     * Define esta organização como ativa no usuário caso ele não tenha nenhuma ativa.
     *
     * @param slug slug da organização a ingressar
     * @param user usuário ingressante
     * @return DTO de resposta da organização ingressada
     * @throws OrganizationNotFoundException se nenhuma organização tiver esse slug
     * @throws AlreadyMemberException        se o usuário já for membro
     */
    @Transactional
    public OrganizationResponseDTO join(String slug, User user) {
        log.info("Usuário {} tentando ingressar na organização com slug '{}'", user.getId(), slug);

        Organization org = orgRepo.findBySlug(slug)
                .orElseThrow(OrganizationNotFoundException::new);

        if (memberRepo.existsByOrganizationIdAndUserId(org.getId(), user.getId())) {
            throw new AlreadyMemberException();
        }

        OrganizationMember member = new OrganizationMember();
        member.setOrganization(org);
        member.setUser(user);
        member.setRole(MemberRole.MEMBER);
        memberRepo.save(member);

        // Só define como ativa se o usuário ainda não tiver organização ativa
        if (user.getActiveOrganizationId() == null) {
            user.setActiveOrganizationId(org.getId());
            userRepo.save(user);
        }

        log.info("Usuário {} ingressou na organização {} como MEMBER", user.getId(), org.getId());
        return toDto(org, MemberRole.MEMBER);
    }

    // endregion

    // region Troca de organização ativa

    /**
     * Troca a organização ativa do usuário (seletor da sidebar).
     *
     * @param organizationId ID da organização a ativar
     * @param user           usuário autenticado
     * @return DTO da organização ativada
     * @throws NotMemberException            se o usuário não for membro da organização
     * @throws OrganizationNotFoundException se a organização não existir
     */
    @Transactional
    public OrganizationResponseDTO switchOrganization(String organizationId, User user) {
        log.info("Usuário {} trocando organização ativa para {}", user.getId(), organizationId);

        Organization org = orgRepo.findById(organizationId)
                .orElseThrow(OrganizationNotFoundException::new);

        OrganizationMember membership = memberRepo
                .findByOrganizationIdAndUserId(organizationId, user.getId())
                .orElseThrow(NotMemberException::new);

        user.setActiveOrganizationId(organizationId);
        userRepo.save(user);

        log.info("Organização ativa do usuário {} atualizada para {}", user.getId(), organizationId);
        return toDto(org, membership.getRole());
    }

    // endregion

    // region Consultas

    /**
     * Retorna todas as organizações às quais o usuário pertence.
     *
     * @param userId ID do usuário
     * @return lista de DTOs das organizações
     */
    public List<OrganizationResponseDTO> getMyOrganizations(String userId) {
        return memberRepo.findByUserId(userId).stream()
                .map(m -> toDto(m.getOrganization(), m.getRole()))
                .toList();
    }

    /**
     * Retorna todos os membros de uma organização.
     * Exige que o solicitante seja membro da organização.
     *
     * @param organizationId ID da organização
     * @param requesterId    ID do usuário solicitante
     * @return lista de DTOs dos membros
     * @throws NotMemberException se o solicitante não for membro
     */
    public List<MemberResponseDTO> getMembers(String organizationId, String requesterId) {
        if (!memberRepo.existsByOrganizationIdAndUserId(organizationId, requesterId)) {
            throw new NotMemberException();
        }
        return memberRepo.findByOrganizationId(organizationId).stream()
                .map(m -> new MemberResponseDTO(
                        m.getUser().getId(),
                        m.getUser().getUsername(),
                        m.getUser().getEmail(),
                        m.getRole(),
                        m.getJoinedAt()
                ))
                .toList();
    }

    // endregion

    // region Remoção de membro

    /**
     * Remove um membro de uma organização.
     * Apenas OWNER ou ADMIN podem remover membros.
     * Um OWNER não pode remover a si mesmo (precisa transferir o ownership antes).
     *
     * @param organizationId ID da organização
     * @param targetUserId   ID do usuário a remover
     * @param requester      usuário solicitante da remoção
     */
    @Transactional
    public void removeMember(String organizationId, String targetUserId, User requester) {
        OrganizationMember requesterMembership = memberRepo
                .findByOrganizationIdAndUserId(organizationId, requester.getId())
                .orElseThrow(NotMemberException::new);

        if (requesterMembership.getRole() == MemberRole.MEMBER) {
            throw new com.psicomanager.api.core.exception.BusinessRuleException(
                    "Apenas OWNER ou ADMIN podem remover membros");
        }

        if (requester.getId().equals(targetUserId)
                && requesterMembership.getRole() == MemberRole.OWNER) {
            throw new com.psicomanager.api.core.exception.BusinessRuleException(
                    "O OWNER não pode remover a si mesmo. Transfira a propriedade primeiro.");
        }

        OrganizationMember target = memberRepo
                .findByOrganizationIdAndUserId(organizationId, targetUserId)
                .orElseThrow(NotMemberException::new);

        memberRepo.delete(target);
        log.info("Usuário {} removido da organização {} por {}", targetUserId, organizationId, requester.getId());

        // Se a organização removida era a ativa do usuário, limpa o campo
        User targetUser = (User) userRepo.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("Usuário alvo não encontrado"));
        if (organizationId.equals(targetUser.getActiveOrganizationId())) {
            // Tenta definir outra organização como ativa automaticamente
            var remaining = memberRepo.findByUserId(targetUserId);
            targetUser.setActiveOrganizationId(
                    remaining.isEmpty() ? null : remaining.get(0).getOrganization().getId()
            );
            userRepo.save(targetUser);
        }
    }

    // endregion

    // region Mapper

    private OrganizationResponseDTO toDto(Organization org, MemberRole role) {
        return new OrganizationResponseDTO(
                org.getId(),
                org.getName(),
                org.getSlug(),
                role,
                org.getCreatedAt()
        );
    }

    // endregion
}
