package com.psicomanager.api.plan.template.dto;

import com.psicomanager.api.schedule.enums.FrequencyEnum;

import java.math.BigDecimal;

public record PlanTemplateResponseDTO(
        String id,
        String title,
        BigDecimal pricePerSession,
        Integer sessionsCount,
        FrequencyEnum frequency,
        BigDecimal totalValue
) {}
