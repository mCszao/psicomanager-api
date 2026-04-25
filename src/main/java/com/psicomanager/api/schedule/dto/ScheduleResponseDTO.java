package com.psicomanager.api.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.psicomanager.api.patient.dto.PatientResumeResponseDTO;
import com.psicomanager.api.plan.dto.PlanResumeResponseDTO;
import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.StageEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScheduleResponseDTO(
        String id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dateStart,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dateEnd,
        String annotations,
        StageEnum stage,
        AttendanceTypeEnum type,
        PatientResumeResponseDTO patient,
        ScheduleRescheduledToDTO rescheduledTo,
        PlanResumeResponseDTO plan,
        BigDecimal sessionValue
) {}
