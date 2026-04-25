package com.psicomanager.api.schedule.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduleRescheduleDTO(
        @NotNull
        LocalDateTime dateStart,
        LocalDateTime dateEnd
) {}
