package com.psicomanager.api.plan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PlanRegisterDTO(
        @NotBlank
        String patientId,

        // opcional — se informado, herda os valores do template
        String planTemplateId,

        // opcional — sobrescreve o título do template
        String title,

        // opcional — sobrescreve o pricePerSession do template
        BigDecimal pricePerSession,

        // opcional — sobrescreve sessionsCount do template
        @Min(1)
        Integer sessionsCount,

        // opcional — sobrescreve frequency do template
        FrequencyEnum frequency,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
        LocalDate adherenceDate,

        // opcional — se não informado e houver frequency + sessionsCount, é calculado automaticamente
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
        LocalDate estimatedEndDate,

        // se true, gera as sessões automaticamente com base em frequency + sessionsCount
        boolean generateSessions,

        // horário de início de cada sessão gerada (obrigatório se generateSessions = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        String sessionStartTime
) {}
