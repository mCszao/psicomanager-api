package com.psicomanager.api.plan.template.dto;

import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PlanTemplateRegisterDTO(
        @NotBlank
        String title,
        @NotNull
        BigDecimal pricePerSession,
        @NotNull
        @Min(1)
        Integer sessionsCount,
        @NotNull
        FrequencyEnum frequency,
        /** Tipo de atendimento padrão para sessões geradas a partir deste template. Opcional. */
        AttendanceTypeEnum attendanceType
) {}
