package com.psicomanager.api.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import com.psicomanager.api.schedule.enums.StageEnum;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduleRegisterDTO(
        @NotBlank
        String patientId,

        @NotNull
        @Future
        LocalDateTime dateStart,

        LocalDateTime dateEnd,

        StageEnum stage,

        AttendanceTypeEnum type,

        // opcional — vínculo com plano existente
        String planId,

        // opcional — frequência para criação em lote sem plano
        FrequencyEnum frequency,

        // opcional — quantidade de sessões a gerar em lote (requer frequency)
        @Min(1)
        Integer sessionsCount
) {}
