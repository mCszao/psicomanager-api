package com.psicomanager.api.plan.mapper;

import com.psicomanager.api.patient.mapper.PatientMapper;
import com.psicomanager.api.plan.dto.PlanResponseDTO;
import com.psicomanager.api.plan.model.Plan;
import com.psicomanager.api.plan.template.dto.PlanTemplateResponseDTO;
import com.psicomanager.api.plan.template.model.PlanTemplate;
import org.springframework.stereotype.Component;

@Component
public class PlanMapper {

    public static PlanTemplateResponseDTO templateToDto(PlanTemplate t) {
        return new PlanTemplateResponseDTO(
                t.getId(),
                t.getTitle(),
                t.getPricePerSession(),
                t.getSessionsCount(),
                t.getFrequency(),
                t.getTotalValue()
        );
    }

    public static PlanResponseDTO toDto(Plan plan) {
        return new PlanResponseDTO(
                plan.getId(),
                PatientMapper.toResumeDto(plan.getPatient()),
                plan.getPlanTemplate() != null ? templateToDto(plan.getPlanTemplate()) : null,
                plan.getTitle(),
                plan.getPricePerSession(),
                plan.getSessionsCount(),
                plan.getFrequency(),
                plan.getTotalValue(),
                plan.getAdherenceDate(),
                plan.getEstimatedEndDate(),
                plan.getStartedAt(),
                plan.getEndedAt(),
                plan.getIsActive()
        );
    }
}
