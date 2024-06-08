package com.psicomanager.api.services;

import com.psicomanager.api.domain.patient.PatientResumeResponseDTO;
import com.psicomanager.api.domain.schedule.Schedule;
import com.psicomanager.api.domain.schedule.ScheduleRegisterDTO;
import com.psicomanager.api.domain.schedule.ScheduleResponseDTO;
import com.psicomanager.api.exceptions.patient.PatientNotFoundException;
import com.psicomanager.api.exceptions.schedule.ScheduleConflictTimeException;
import com.psicomanager.api.exceptions.schedule.ScheduleNotFoundException;
import com.psicomanager.api.repositories.PatientRepository;
import com.psicomanager.api.repositories.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Transactional
    public void createSchedule(ScheduleRegisterDTO dto) {
        log.info("Verificando se nova consulta possúi conflito de horário com alguma existente");
        var schedules = scheduleRepo.getScheduleBetweenStartEnd(dto.dateStart(), dto.dateStart().plusHours(1));
        if (schedules.isEmpty()) {
            log.info("Buscando informações do paciente de id"+ dto.patientId());
            var patient = patientRepo.findById(dto.patientId()).orElseThrow(() -> new PatientNotFoundException("Paciente informado não encontrado"));
            Schedule formedSchedule = new Schedule(dto, patient);
            log.info("Salvando nova consulta do paciente de id "+ dto.patientId());
            scheduleRepo.save(formedSchedule);
            return;
        }
        throw new ScheduleConflictTimeException("Já existe uma sessão no período informado");
    }

    public List<ScheduleResponseDTO> getAllSchedules() {
        log.info("Buscando por todas as consultas");
        return scheduleRepo.findAll().stream().map((schedule) -> {
            var patient = schedule.getPatient();
            return new ScheduleResponseDTO(
                    schedule.getId(),
                    schedule.getDateStart(),
                    schedule.getDateEnd(),
                    schedule.getAnnotations(),
                    schedule.getStage(),
                    new PatientResumeResponseDTO(patient.getId(), patient.getName(), patient.getPhone())
            );
        }).toList();
    }

    public List<ScheduleResponseDTO> getAllByPatientId(String patientId){
        log.info("Verificando informações do paciente de id "+ patientId);
        patientRepo.findById(patientId).orElseThrow(() -> new PatientNotFoundException("Paciente informado não possuí registro"));
        log.info("Retornando consultas do paciente de id "+ patientId);
        return scheduleRepo.findByPatientId(patientId).stream().map((schedule) -> {
            var patient = schedule.getPatient();
            return new ScheduleResponseDTO(
                    schedule.getId(),
                    schedule.getDateStart(),
                    schedule.getDateEnd(),
                    schedule.getAnnotations(),
                    schedule.getStage(),
                    new PatientResumeResponseDTO(patient.getId(), patient.getName(), patient.getPhone())
            );
        }).toList();
    }

    public ScheduleResponseDTO getScheduleById(String id){
        log.info("Buscando informações da consulta de id"+ id);
        var schedule = scheduleRepo.findById(id).orElseThrow(() -> new ScheduleNotFoundException("Agendamento com id enviado não possuí registro"));
        log.info("Retornando consulta");
        return ScheduleResponseDTO.of(schedule);
    }
}
