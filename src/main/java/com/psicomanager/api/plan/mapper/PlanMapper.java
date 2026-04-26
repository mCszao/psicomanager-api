package com.psicomanager.api.plan.mapper;

import com.psicomanager.api.patient.mapper.PatientMapper;
import com.psicomanager.api.plan.dto.PlanResponseDTO;
import com.psicomanager.api.plan.model.Plan;
import com.psicomanager.api.plan.template.dto.PlanTemplateResponseDTO;
import com.psicomanager.api.plan.template.model.PlanTemplate;
import org.springframework.stereotype.Component;

/**
 * Responsável por converter entidades de plano em DTOs de resposta.
 */
@Component
public class PlanMapper {

    // region Template

    /**
     * Converte um {@link PlanTemplate} em seu DTO de resposta.
     *
     * @param t template a ser convertido
     * @return DTO de resposta do template
     */
    public static PlanTemplateResponseDTO templateToDto(PlanTemplate t) {
        return new PlanTemplateResponseDTO(
                t.getId(),
                t.getTitle(),
                t.getPricePerSession(),
                t.getSessionsCount(),
                t.getFrequency(),
                t.getTotalValue(),
                t.getAttendanceType()
        );
    }

    // endregion

    // region Plan

    /**
     * Converte um {@link Plan} em seu DTO de resposta completo.
     *
     * @param plan plano a ser convertido
     * @return DTO de resposta do plano
     */
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
                plan.getAttendanceType(),
                plan.getAdherenceDate(),
                plan.getEstimatedEndDate(),
                plan.getStartedAt(),
                plan.getEndedAt(),
                plan.getIsActive(),
                plan.getIsContinuous()
        );
    }

    // endregion
}
