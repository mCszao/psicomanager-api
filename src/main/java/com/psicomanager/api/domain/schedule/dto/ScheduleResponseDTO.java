package com.psicomanager.api.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.psicomanager.api.domain.patient.dto.PatientResumeResponseDTO;
import com.psicomanager.api.domain.patient.mapper.PatientMapper;
import com.psicomanager.api.domain.schedule.enums.StageEnum;
import com.psicomanager.api.domain.schedule.model.Schedule;

import java.time.LocalDateTime;

public record ScheduleResponseDTO(
        String id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dateStart,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dateEnd,
        String annotations,
        StageEnum stage,
        PatientResumeResponseDTO patient

) {
}
