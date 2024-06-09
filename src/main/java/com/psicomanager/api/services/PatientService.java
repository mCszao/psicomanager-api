package com.psicomanager.api.services;

import com.psicomanager.api.domain.patient.model.Patient;
import com.psicomanager.api.domain.patient.dto.PatientRegisterDTO;
import com.psicomanager.api.domain.patient.dto.PatientResponseDTO;
import com.psicomanager.api.domain.patient.dto.PatientResumeResponseDTO;
import com.psicomanager.api.domain.patient.exception.DuplicatePatientEntryException;
import com.psicomanager.api.domain.patient.exception.PatientNotFoundException;
import com.psicomanager.api.repositories.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepo;

    @Transactional
    public void register(PatientRegisterDTO dto){
        log.info("Validando informações enviadas");
        if(patientRepo.findByEmail(dto.email() == null ? "Não cadastrado" : dto.email()) != null) throw new DuplicatePatientEntryException("Email do paciente");
        if(patientRepo.findByPhone(dto.phone()) != null) throw new DuplicatePatientEntryException("Telefone do paciente");
        if(patientRepo.findByCpf(dto.cpf()) != null) throw new DuplicatePatientEntryException("Cpf do paciente");
        log.info("Salvando novo paciente");
        patientRepo.save(new Patient(dto));
    }

    public List<PatientResponseDTO> getAllPatientsComplete(){
        log.info("Buscando pacientes");
        return patientRepo.findAll().stream().map(PatientResponseDTO::of).toList();
    }
    public List<PatientResumeResponseDTO> getAllPatientsResume(){
        log.info("Buscando pacientes resumidos");
        return patientRepo.findAll().stream().map(patient -> new PatientResumeResponseDTO(patient.getId(),patient.getName(), patient.getPhone())).toList();
    }

    public PatientResponseDTO getDetailsById(String id){
        log.info("Buscando informações do paciente de id"+ id);
        var patient = patientRepo.findById(id).orElseThrow(() -> new PatientNotFoundException("Id do paciente informado não possui registro"));
        log.info("Retornando paciente");
        return PatientResponseDTO.of(patient);
    }
}
