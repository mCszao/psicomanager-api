package com.psicomanager.api.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.psicomanager.api.domain.schedule.enums.StageEnum;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduleRegisterDTO(
        @NotBlank
        String patientId,
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        @Future
        LocalDateTime dateStart,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dateEnd,

        StageEnum stage
) {}
