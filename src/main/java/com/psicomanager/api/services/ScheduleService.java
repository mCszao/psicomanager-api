package com.psicomanager.api.services;

import com.psicomanager.api.domain.patient.Patient;
import com.psicomanager.api.domain.schedule.Schedule;
import com.psicomanager.api.domain.schedule.ScheduleRegisterDTO;
import com.psicomanager.api.repositories.PatientRepository;
import com.psicomanager.api.repositories.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    private PatientRepository patientRepo;

    public boolean save(ScheduleRegisterDTO dto){
        if(!dto.patientId().equals("")) {
            var patient =  patientRepo.findById(dto.patientId()).orElseThrow(() -> new RuntimeException("Paciente com id informado não encontrado"));
            Schedule formedSchedule = new Schedule(dto, patient);
            scheduleRepo.save(formedSchedule);
            return true;
        }
        return false;
    }
}
