package com.psicomanager.api.services;

import com.psicomanager.api.domain.schedule.mapper.ScheduleMapper;
import com.psicomanager.api.repositories.schedule.model.Schedule;
import com.psicomanager.api.domain.schedule.dto.ScheduleRegisterDTO;
import com.psicomanager.api.domain.schedule.dto.ScheduleResponseDTO;
import com.psicomanager.api.domain.patient.exception.PatientNotFoundException;
import com.psicomanager.api.domain.schedule.exception.ScheduleAlreadyConcludedException;
import com.psicomanager.api.domain.schedule.exception.ScheduleAlreadyCancelledException;
import com.psicomanager.api.domain.schedule.exception.ScheduleAlreadyAbsentException;
import com.psicomanager.api.domain.schedule.exception.ScheduleAlreadyRescheduledException;
import com.psicomanager.api.domain.schedule.dto.ScheduleRescheduleDTO;
import com.psicomanager.api.domain.schedule.exception.ScheduleConflictTimeException;
import com.psicomanager.api.domain.schedule.exception.ScheduleNotFoundException;
import com.psicomanager.api.domain.schedule.enums.StageEnum;
import com.psicomanager.api.repositories.PatientRepository;
import com.psicomanager.api.repositories.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private ScheduleMapper mapper;


    private LocalDateTime resolveEnd(LocalDateTime start, LocalDateTime end) {
        return end != null ? end : start.plusHours(1);
    }

    private void assertNoConflict(LocalDateTime start, LocalDateTime effectiveEnd, String excludeId) {
        var conflicts = scheduleRepo.findConflictingSchedules(start, effectiveEnd, excludeId);
        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictTimeException();
        }
    }


    @Transactional
    public void createSchedule(ScheduleRegisterDTO dto) {
        log.info("Verificando conflito de horário para nova consulta");
        assertNoConflict(dto.dateStart(), resolveEnd(dto.dateStart(), dto.dateEnd()), null);

        log.info("Buscando informações do paciente de id " + dto.patientId());
        var patient = patientRepo.findById(dto.patientId()).orElseThrow(PatientNotFoundException::new);

        Schedule formedSchedule = mapper.dtoToEntity(dto, patient);
        log.info("Salvando nova consulta do paciente de id " + dto.patientId());
        scheduleRepo.save(formedSchedule);
    }

    public List<ScheduleResponseDTO> getAllSchedules() {
        log.info("Buscando por todas as consultas");
        return scheduleRepo.findAll().stream().map(ScheduleMapper::toDto).toList();
    }

    public List<ScheduleResponseDTO> getAllByPatientId(String patientId) {
        log.info("Verificando informações do paciente de id " + patientId);
        patientRepo.findById(patientId).orElseThrow(PatientNotFoundException::new);
        log.info("Retornando consultas do paciente de id " + patientId);
        return scheduleRepo.findByPatientId(patientId).stream().map(ScheduleMapper::toDto).toList();
    }

    public ScheduleResponseDTO getScheduleById(String id) {
        log.info("Buscando informações da consulta de id " + id);
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        log.info("Retornando consulta");
        return ScheduleMapper.toDto(schedule);
    }

    @Transactional
    public void concludeSession(String id) {
        log.info("Buscando sessão de id " + id + " para conclusão");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);

        if (schedule.getStage() != StageEnum.OPENED) {
            throw new ScheduleAlreadyConcludedException();
        }

        schedule.setStage(StageEnum.CONCLUDED);
        schedule.setDateEnd(LocalDateTime.now());
        scheduleRepo.save(schedule);
        log.info("Sessão de id " + id + " concluída com sucesso");
    }

    @Transactional
    public void cancelSession(String id) {
        log.info("Buscando sessão de id " + id + " para cancelamento");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);

        if (schedule.getStage() != StageEnum.OPENED) {
            throw new ScheduleAlreadyCancelledException();
        }

        schedule.setStage(StageEnum.CANCELLED);
        scheduleRepo.save(schedule);
        log.info("Sessão de id " + id + " cancelada com sucesso");
    }

    @Transactional
    public void markAsAbsent(String id) {
        log.info("Buscando sessão de id " + id + " para marcar falta");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);

        if (schedule.getStage() != StageEnum.OPENED) {
            throw new ScheduleAlreadyAbsentException();
        }

        schedule.setStage(StageEnum.ABSENT);
        scheduleRepo.save(schedule);
        log.info("Sessão de id " + id + " marcada como falta com sucesso");
    }

    @Transactional
    public void rescheduleSession(String id, ScheduleRescheduleDTO dto) {
        log.info("Buscando sessão de id " + id + " para reagendamento");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);

        if (schedule.getStage() != StageEnum.OPENED) {
            throw new ScheduleAlreadyRescheduledException();
        }

        LocalDateTime newEnd = resolveEnd(dto.dateStart(), dto.dateEnd());
        log.info("Verificando conflito de horário para reagendamento");
        assertNoConflict(dto.dateStart(), newEnd, id);

        Schedule newSchedule = new Schedule();
        newSchedule.setPatient(schedule.getPatient());
        newSchedule.setDateStart(dto.dateStart());
        newSchedule.setDateEnd(dto.dateEnd() != null ? dto.dateEnd() : dto.dateStart().plusHours(1));
        newSchedule.setStage(StageEnum.OPENED);
        newSchedule.setType(schedule.getType());
        log.info("Salvando nova sessão reagendada para o paciente de id " + schedule.getPatient().getId());
        scheduleRepo.save(newSchedule);

        schedule.setStage(StageEnum.RESCHEDULED);
        schedule.setRescheduledTo(newSchedule);
        scheduleRepo.save(schedule);
        log.info("Sessão de id " + id + " marcada como reagendada com sucesso");
    }
}
