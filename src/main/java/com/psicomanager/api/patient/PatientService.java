package com.psicomanager.api.patient;

import com.psicomanager.api.patient.dto.PatientRegisterDTO;
import com.psicomanager.api.patient.dto.PatientResponseDTO;
import com.psicomanager.api.patient.dto.PatientResumeResponseDTO;
import com.psicomanager.api.patient.exception.DuplicatePatientEntryException;
import com.psicomanager.api.patient.exception.PatientNotFoundException;
import com.psicomanager.api.patient.mapper.PatientMapper;
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
    public void register(PatientRegisterDTO dto) {
        log.info("Validando informações enviadas");
        if (patientRepo.findByEmail(dto.email() == null ? "Não cadastrado" : dto.email()) != null)
            throw new DuplicatePatientEntryException("Email do paciente");
        if (patientRepo.findByPhone(dto.phone()) != null)
            throw new DuplicatePatientEntryException("Telefone do paciente");
        if (patientRepo.findByCpf(dto.cpf()) != null)
            throw new DuplicatePatientEntryException("Cpf do paciente");
        if (patientRepo.findByName(dto.name()) != null)
            throw new DuplicatePatientEntryException("Nome do paciente");
        log.info("Salvando novo paciente");
        var patient = mapper.dtoToEntity(dto);
        patientRepo.save(patient);
    }

    @Transactional
    public List<PatientResponseDTO> getAllPatientsComplete() {
        log.info("Buscando pacientes");
        return patientRepo.findAll().stream().map(PatientMapper::toDto).toList();
    }

    public List<PatientResumeResponseDTO> getAllPatientsResume() {
        log.info("Buscando pacientes resumidos");
        return patientRepo.findAll().stream().map(PatientMapper::toResumeDto).toList();
    }

    @Transactional
    public PatientResponseDTO getDetailsById(String id) {
        log.info("Buscando informações do paciente de id " + id);
        var patient = patientRepo.findById(id).orElseThrow(PatientNotFoundException::new);
        log.info("Retornando paciente");
        return PatientMapper.toDto(patient);
    }
}
