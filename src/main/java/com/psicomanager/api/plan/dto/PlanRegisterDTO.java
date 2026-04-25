package com.psicomanager.api.plan.dto;

import com.psicomanager.api.schedule.enums.FrequencyEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload de registro de um novo plano de atendimento para um paciente.
 * <p>
 * Quando {@code planTemplateId} é informado, os campos {@code pricePerSession},
 * {@code sessionsCount} e {@code frequency} são herdados do template e só
 * precisam ser enviados caso o psicólogo queira sobrescrevê-los para este paciente.
 * </p>
 * <p>
 * Quando {@code generateSessions} é {@code true}, o campo {@code sessionStartTime}
 * torna-se obrigatório e as sessões serão geradas automaticamente com base em
 * {@code frequency} e {@code sessionsCount} a partir de {@code adherenceDate}.
 * </p>
 */
public record PlanRegisterDTO(

        @NotBlank
        String patientId,

        /** ID do template a ser usado como base. Opcional. */
        String planTemplateId,

        /** Título do plano. Opcional — sobrescreve o título do template se informado. */
        String title,

        /** Valor por sessão. Opcional — sobrescreve o valor do template se informado. */
        BigDecimal pricePerSession,

        /** Quantidade de sessões. Opcional — sobrescreve o valor do template se informado. */
        @Min(1)
        Integer sessionsCount,

        /** Frequência das sessões. Opcional — sobrescreve o valor do template se informado. */
        FrequencyEnum frequency,

        @NotNull
        LocalDate adherenceDate,

        /**
         * Data estimada de encerramento. Opcional.
         * Quando não informada e houver {@code frequency} + {@code sessionsCount},
         * é calculada automaticamente.
         */
        LocalDate estimatedEndDate,

        /**
         * Quando {@code true}, gera as sessões automaticamente com base em
         * {@code frequency} e {@code sessionsCount} a partir de {@code adherenceDate}.
         * Requer que {@code sessionStartTime} seja informado.
         */
        boolean generateSessions,

        /**
         * Horário de início de cada sessão gerada (formato {@code HH:mm}).
         * Obrigatório quando {@code generateSessions} é {@code true}.
         */
        String sessionStartTime

) {}
