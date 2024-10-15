package com.psicomanager.api.domain.schedule.mapper;

import com.psicomanager.api.domain.patient.mapper.PatientMapper;
import com.psicomanager.api.repositories.patient.model.Patient;
import com.psicomanager.api.domain.schedule.dto.ScheduleRegisterDTO;
import com.psicomanager.api.domain.schedule.dto.ScheduleResponseDTO;
import com.psicomanager.api.domain.schedule.enums.StageEnum;
import com.psicomanager.api.repositories.schedule.model.Schedule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class ScheduleMapper {
    public Schedule dtoToEntity(ScheduleRegisterDTO dto, Patient patient){
        var schedule = new Schedule();
        if(patient.getId() != null){
             schedule.setPatient(patient);
        }
        schedule.setDateStart(dto.dateStart());
        LocalDateTime end = dto.dateEnd() == null ? dto.dateStart().plusHours(BigDecimal.ONE.toBigInteger().longValue()) : dto.dateEnd();
        schedule.setDateEnd(end);
        StageEnum stage = dto.stage() == null ? StageEnum.OPENED : dto.stage();
        schedule.setStage(stage);
        return schedule;
    }

    public static ScheduleResponseDTO toDto(Schedule schedule){
        return new ScheduleResponseDTO(
                schedule.getId(),
                schedule.getDateStart(),
                schedule.getDateEnd(),
                schedule.getAnnotations(),
                schedule.getStage(),
                PatientMapper.toResumeDto(schedule.getPatient())
        );
    }
}
