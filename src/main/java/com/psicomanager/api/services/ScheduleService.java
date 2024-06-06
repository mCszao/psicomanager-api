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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Transactional
    public void createSchedule(ScheduleRegisterDTO dto) {
        var schedules = scheduleRepo.getScheduleBetweenStartEnd(dto.dateStart(), dto.dateStart().plusHours(1));
        if (schedules.isEmpty()) {
            var patient = patientRepo.findById(dto.patientId()).orElseThrow(() -> new PatientNotFoundException("Paciente informado não encontrado"));
            Schedule formedSchedule = new Schedule(dto, patient);
            scheduleRepo.save(formedSchedule);
            return;
        }
        throw new ScheduleConflictTimeException("Já existe uma sessão no período informado");
    }

    public List<ScheduleResponseDTO> getAllSchedules() {
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
        patientRepo.findById(patientId).orElseThrow(() -> new PatientNotFoundException("Paciente informado não possuí registro"));
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
        var schedule = scheduleRepo.findById(id).orElseThrow(() -> new ScheduleNotFoundException("Agendamento com id enviado não possuí registro"));
        return ScheduleResponseDTO.of(schedule);
    }
}
