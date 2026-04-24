package com.psicomanager.api.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ScheduleRescheduledToDTO(
        String id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dateStart,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dateEnd
) {}
