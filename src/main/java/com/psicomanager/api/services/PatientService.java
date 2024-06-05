package com.psicomanager.api.services;

import com.psicomanager.api.domain.patient.Patient;
import com.psicomanager.api.domain.patient.PatientRegisterDTO;
import com.psicomanager.api.exceptions.patient.DuplicatePatientEntryException;
import com.psicomanager.api.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepo;

    public void register(PatientRegisterDTO dto){
        if(patientRepo.findByEmail(dto.email() == null ? "Não cadastrado" : dto.email()) != null) throw new DuplicatePatientEntryException("Patient email");
        if(patientRepo.findByPhone(dto.phone()) != null) throw new DuplicatePatientEntryException("Patient phone");
        if(patientRepo.findByCpf(dto.cpf()) != null) throw new DuplicatePatientEntryException("Patient cpf");
        patientRepo.save(new Patient(dto));
    }
}
