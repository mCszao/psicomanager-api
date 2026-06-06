package com.psicomanager.api.organization;

import com.psicomanager.api.core.dto.BaseResponse;
import com.psicomanager.api.organization.dto.CreateOrganizationDTO;
import com.psicomanager.api.organization.dto.MemberResponseDTO;
import com.psicomanager.api.organization.dto.OrganizationResponseDTO;
import com.psicomanager.api.user.model.User;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de gestão de organizações e membros.
 *
 * <p>Rotas sem prefixo de tenant — acessíveis mesmo sem organização ativa,
 * pois são usadas no fluxo de onboarding.</p>
 */
@RestController
@RequestMapping("/organizations")
@Slf4j
public class OrganizationController {

    @Autowired
    private OrganizationService orgService;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /** Cria uma nova organização e torna o usuário OWNER dela. */
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<OrganizationResponseDTO>> create(
            @RequestBody @Valid CreateOrganizationDTO body
    ) {
        log.info("POST: /organizations/create");
        return ResponseEntity.ok(new BaseResponse<>(true, orgService.create(body, currentUser())));
    }

    /** Ingressa na organização identificada pelo slug (código de convite). */
    @PostMapping("/join/{slug}")
    public ResponseEntity<BaseResponse<OrganizationResponseDTO>> join(@PathVariable String slug) {
        log.info("POST: /organizations/join/{}", slug);
        return ResponseEntity.ok(new BaseResponse<>(true, orgService.join(slug, currentUser())));
    }

    /** Troca a organização ativa na sidebar. */
    @PatchMapping("/switch/{organizationId}")
    public ResponseEntity<BaseResponse<OrganizationResponseDTO>> switchOrg(
            @PathVariable String organizationId
    ) {
        log.info("PATCH: /organizations/switch/{}", organizationId);
        return ResponseEntity.ok(new BaseResponse<>(true, orgService.switchOrganization(organizationId, currentUser())));
    }

    /** Retorna todas as organizações do usuário autenticado. */
    @GetMapping("/my")
    public ResponseEntity<BaseResponse<List<OrganizationResponseDTO>>> myOrganizations() {
        log.info("GET: /organizations/my");
        return ResponseEntity.ok(new BaseResponse<>(true, orgService.getMyOrganizations(currentUser().getId())));
    }

    /** Lista os membros de uma organização (requer ser membro). */
    @GetMapping("/{organizationId}/members")
    public ResponseEntity<BaseResponse<List<MemberResponseDTO>>> getMembers(
            @PathVariable String organizationId
    ) {
        log.info("GET: /organizations/{}/members", organizationId);
        return ResponseEntity.ok(new BaseResponse<>(true, orgService.getMembers(organizationId, currentUser().getId())));
    }

    /** Remove um membro da organização (requer ser OWNER ou ADMIN). */
    @DeleteMapping("/{organizationId}/members/{userId}")
    public ResponseEntity<BaseResponse<String>> removeMember(
            @PathVariable String organizationId,
            @PathVariable String userId
    ) {
        log.info("DELETE: /organizations/{}/members/{}", organizationId, userId);
        orgService.removeMember(organizationId, userId, currentUser());
        return ResponseEntity.ok(new BaseResponse<>(true, "Membro removido com sucesso!"));
    }
}
