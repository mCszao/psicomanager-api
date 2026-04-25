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

@Service
@Slf4j
public class PlanTemplateService {

    @Autowired
    private PlanTemplateRepository planTemplateRepo;

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

    public List<PlanTemplateResponseDTO> getAll() {
        return planTemplateRepo.findAll().stream().map(PlanMapper::templateToDto).toList();
    }

    public PlanTemplateResponseDTO getById(String id) {
        var template = planTemplateRepo.findById(id).orElseThrow(PlanTemplateNotFoundException::new);
        return PlanMapper.templateToDto(template);
    }

    @Transactional
    public void delete(String id) {
        log.info("Removendo template de plano de id " + id);
        planTemplateRepo.findById(id).orElseThrow(PlanTemplateNotFoundException::new);
        planTemplateRepo.deleteById(id);
    }
}
