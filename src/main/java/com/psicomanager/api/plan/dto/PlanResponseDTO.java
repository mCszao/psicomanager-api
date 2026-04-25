package com.psicomanager.api.plan.dto;

import com.psicomanager.api.patient.dto.PatientResumeResponseDTO;
import com.psicomanager.api.plan.template.dto.PlanTemplateResponseDTO;
import com.psicomanager.api.schedule.enums.FrequencyEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PlanResponseDTO(
        String id,
        PatientResumeResponseDTO patient,
        PlanTemplateResponseDTO planTemplate,
        String title,
        BigDecimal pricePerSession,
        Integer sessionsCount,
        FrequencyEnum frequency,
        BigDecimal totalValue,
        LocalDate adherenceDate,
        LocalDate estimatedEndDate,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Boolean isActive,
        Boolean isContinuous
) {}
