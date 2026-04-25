package com.psicomanager.api.plan;

import com.psicomanager.api.plan.exception.PlanTemplateNotFoundException;
import com.psicomanager.api.plan.mapper.PlanMapper;
import com.psicomanager.api.plan.template.dto.PlanTemplateRegisterDTO;
import com.psicomanager.api.plan.template.dto.PlanTemplateResponseDTO;
import com.psicomanager.api.plan.template.model.PlanTemplate;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Gerencia os templates reutilizáveis de plano de atendimento.
 * <p>
 * Templates são modelos globais do psicólogo que definem preço, frequência
 * e quantidade de sessões. Ao criar um plano para um paciente, o psicólogo
 * pode optar por partir de um template e sobrescrever valores individualmente.
 * </p>
 */
@Service
@Slf4j
public class PlanTemplateService {

    // region Dependências

    @Autowired
    private PlanTemplateRepository planTemplateRepo;

    // endregion

    // region Criação

    /**
     * Cria um novo template de plano.
     *
     * @param dto payload com os dados do template
     */
    @Transactional
    public void create(PlanTemplateRegisterDTO dto) {
        log.info("Criando template de plano: " + dto.title());
        PlanTemplate template = new PlanTemplate();
        template.setTitle(dto.title());
        template.setPricePerSession(dto.pricePerSession());
        template.setSessionsCount(dto.sessionsCount());
        template.setFrequency(dto.frequency());
        planTemplateRepo.save(template);
        log.info("Template de plano salvo com sucesso");
    }

    // endregion

    // region Consultas

    /**
     * Retorna todos os templates cadastrados.
     *
     * @return lista de DTOs de templates
     */
    public List<PlanTemplateResponseDTO> getAll() {
        return planTemplateRepo.findAll().stream().map(PlanMapper::templateToDto).toList();
    }

    /**
     * Retorna um template pelo seu ID.
     *
     * @param id ID do template
     * @return DTO de resposta do template
     * @throws PlanTemplateNotFoundException se o template não for encontrado
     */
    public PlanTemplateResponseDTO getById(String id) {
        var template = planTemplateRepo.findById(id).orElseThrow(PlanTemplateNotFoundException::new);
        return PlanMapper.templateToDto(template);
    }

    // endregion

    // region Remoção

    /**
     * Remove um template pelo seu ID.
     *
     * @param id ID do template a remover
     * @throws PlanTemplateNotFoundException se o template não for encontrado
     */
    @Transactional
    public void delete(String id) {
        log.info("Removendo template de plano de id " + id);
        planTemplateRepo.findById(id).orElseThrow(PlanTemplateNotFoundException::new);
        planTemplateRepo.deleteById(id);
    }

    // endregion
}
