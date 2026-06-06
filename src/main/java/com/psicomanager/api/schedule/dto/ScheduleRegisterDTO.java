package com.psicomanager.api.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dateStart,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
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
