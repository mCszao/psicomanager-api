package com.psicomanager.api.domain.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.psicomanager.api.domain.patient.PatientResumeResponseDTO;

import java.time.LocalDateTime;

public record ScheduleResponseDTO(
        String id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dateStart,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:ss:ss")
        LocalDateTime dateEnd,
        String annotations,
        StageEnum stage,
        PatientResumeResponseDTO patient

) {
}
