package com.psicomanager.api.services;

import com.psicomanager.api.domain.patient.Patient;
import com.psicomanager.api.domain.schedule.Schedule;
import com.psicomanager.api.domain.schedule.ScheduleRegisterDTO;
import com.psicomanager.api.domain.schedule.StageEnum;
import com.psicomanager.api.exceptions.patient.PatientNotFound;
import com.psicomanager.api.exceptions.schedule.ScheduleConflictTimeException;
import com.psicomanager.api.repositories.PatientRepository;
import com.psicomanager.api.repositories.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    private PatientRepository patientRepo;

    public void createSchedule(ScheduleRegisterDTO dto){
        var schedules = scheduleRepo.getScheduleBetweenStartEnd(dto.dateStart(), dto.dateStart().plusHours(1));
        if(schedules.isEmpty()) {
            var patient =  patientRepo.findById(dto.patientId()).orElseThrow(() -> new PatientNotFound("Paciente informado não encontrado"));
            Schedule formedSchedule = new Schedule(dto, patient);
            scheduleRepo.save(formedSchedule);
            return;
        }
        throw new ScheduleConflictTimeException("Já existe uma sessão no período informado");
    }
}
