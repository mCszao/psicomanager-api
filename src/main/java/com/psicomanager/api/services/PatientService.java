package com.psicomanager.api.services;

import com.psicomanager.api.domain.patient.mapper.PatientMapper;
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

    @Autowired
    private PatientMapper mapper;
    @Transactional
    public void register(PatientRegisterDTO dto){
        log.info("Validando informações enviadas");
        if(patientRepo.findByEmail(dto.email() == null ? "Não cadastrado" : dto.email()) != null) throw new DuplicatePatientEntryException("Email do paciente");
        if(patientRepo.findByPhone(dto.phone()) != null) throw new DuplicatePatientEntryException("Telefone do paciente");
        if(patientRepo.findByCpf(dto.cpf()) != null) throw new DuplicatePatientEntryException("Cpf do paciente");
        log.info("Salvando novo paciente");
        var patient = mapper.dtoToEntity(dto);
        patientRepo.save(patient);
    }

    public List<PatientResponseDTO> getAllPatientsComplete(){
        log.info("Buscando pacientes");
        return patientRepo.findAll().stream().map(PatientMapper::toDto).toList();
    }
    public List<PatientResumeResponseDTO> getAllPatientsResume(){
        log.info("Buscando pacientes resumidos");
        return patientRepo.findAll().stream().map(PatientMapper::toResumeDto).toList();
    }

    public PatientResponseDTO getDetailsById(String id){
        log.info("Buscando informações do paciente de id"+ id);
        var patient = patientRepo.findById(id).orElseThrow(() -> new PatientNotFoundException("Id do paciente informado não possui registro"));
        log.info("Retornando paciente");
        return PatientMapper.toDto(patient);
    }
}
