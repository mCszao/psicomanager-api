package com.psicomanager.api.schedule.mapper;

import com.psicomanager.api.patient.dto.PatientResumeResponseDTO;
import com.psicomanager.api.patient.mapper.PatientMapper;
import com.psicomanager.api.patient.model.Patient;
import com.psicomanager.api.plan.dto.PlanResumeResponseDTO;
import com.psicomanager.api.schedule.dto.ScheduleRegisterDTO;
import com.psicomanager.api.schedule.dto.ScheduleResponseDTO;
import com.psicomanager.api.schedule.dto.ScheduleRescheduledToDTO;
import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.StageEnum;
import com.psicomanager.api.schedule.model.Schedule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class ScheduleMapper {

    public Schedule dtoToEntity(ScheduleRegisterDTO dto, Patient patient) {
        var schedule = new Schedule();
        if (patient.getId() != null) {
            schedule.setPatient(patient);
        }
        schedule.setDateStart(dto.dateStart());
        LocalDateTime end = dto.dateEnd() == null
                ? dto.dateStart().plusHours(BigDecimal.ONE.toBigInteger().longValue())
                : dto.dateEnd();
        schedule.setDateEnd(end);
        StageEnum stage = dto.stage() == null ? StageEnum.OPENED : dto.stage();
        schedule.setStage(stage);
        AttendanceTypeEnum type = dto.type() == null ? AttendanceTypeEnum.PRESENTIAL : dto.type();
        schedule.setType(type);
        return schedule;
    }

    public static ScheduleResponseDTO toDto(Schedule schedule) {
        ScheduleRescheduledToDTO rescheduledTo = schedule.getRescheduledTo() != null
                ? new ScheduleRescheduledToDTO(
                        schedule.getRescheduledTo().getId(),
                        schedule.getRescheduledTo().getDateStart(),
                        schedule.getRescheduledTo().getDateEnd())
                : null;

        PlanResumeResponseDTO planResume = schedule.getPlan() != null
                ? new PlanResumeResponseDTO(
                        schedule.getPlan().getId(),
                        schedule.getPlan().getTitle() != null
                                ? schedule.getPlan().getTitle()
                                : schedule.getPlan().getPlanTemplate() != null
                                        ? schedule.getPlan().getPlanTemplate().getTitle()
                                        : null)
                : null;

        return new ScheduleResponseDTO(
                schedule.getId(),
                schedule.getDateStart(),
                schedule.getDateEnd(),
                schedule.getAnnotations(),
                schedule.getStage(),
                schedule.getType(),
                PatientMapper.toResumeDto(schedule.getPatient()),
                rescheduledTo,
                planResume,
                schedule.getSessionValue()
        );
    }
}
