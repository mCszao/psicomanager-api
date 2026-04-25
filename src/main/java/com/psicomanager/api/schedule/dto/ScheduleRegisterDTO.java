package com.psicomanager.api.schedule.dto;

import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import com.psicomanager.api.schedule.enums.StageEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payload de registro de uma nova sessão de atendimento avulsa.
 * <p>
 * Para criação de múltiplas sessões com frequência, utilize o fluxo de planos.
 * </p>
 */
public record ScheduleRegisterDTO(

        @NotBlank
        String patientId,

        @NotNull
        LocalDateTime dateStart,

        LocalDateTime dateEnd,

        StageEnum stage,

        AttendanceTypeEnum type,

        /** Vínculo com um plano existente. Opcional. */
        String planId,

        /**
         * Frequência para criação em lote via fluxo de planos.
         * Quando informado junto com {@code sessionsCount}, cria múltiplas sessões espaçadas.
         */
        FrequencyEnum frequency,

        /**
         * Quantidade de sessões a criar em lote.
         * Requer {@code frequency}.
         */
        Integer sessionsCount,

        /**
         * Valor cobrado por esta sessão.
         * Quando vinculada a um plano, é preenchido automaticamente com o
         * {@code pricePerSession} do plano caso não seja informado.
         */
        BigDecimal sessionValue

) {}
