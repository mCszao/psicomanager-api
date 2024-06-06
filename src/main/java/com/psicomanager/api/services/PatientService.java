package com.psicomanager.api.services;

import com.psicomanager.api.domain.address.Address;
import com.psicomanager.api.domain.address.AddressOnPatientDTO;
import com.psicomanager.api.domain.patient.Patient;
import com.psicomanager.api.domain.patient.PatientRegisterDTO;
import com.psicomanager.api.domain.patient.PatientResponseDTO;
import com.psicomanager.api.domain.patient.PatientResumeResponseDTO;
import com.psicomanager.api.exceptions.patient.DuplicatePatientEntryException;
import com.psicomanager.api.exceptions.patient.PatientNotFoundException;
import com.psicomanager.api.repositories.AddressRepository;
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
    private AddressRepository addressRepo;

    @Transactional
    public void register(PatientRegisterDTO dto){
        if(patientRepo.findByEmail(dto.email() == null ? "Não cadastrado" : dto.email()) != null) throw new DuplicatePatientEntryException("Email do paciente");
        if(patientRepo.findByPhone(dto.phone()) != null) throw new DuplicatePatientEntryException("Telefone do paciente");
        if(patientRepo.findByCpf(dto.cpf()) != null) throw new DuplicatePatientEntryException("Cpf do paciente");
        patientRepo.save(new Patient(dto));
    }

    public List<PatientResponseDTO> getAllPatientsComplete(){
        log.info("Pesquisando por todos os usuários");
        return patientRepo.findAll().stream().map(patient ->{
            return new PatientResponseDTO(patient.getId(),patient.getName(), patient.getEmail(), patient.getPhone(), patient.getCpf(), patient.getBirthdayDate(), patient.getAddresses());
        }).toList();
    }
    public List<PatientResumeResponseDTO> getAllPatientsResume(){
        return patientRepo.findAll().stream().map(patient ->{
            return new PatientResumeResponseDTO(patient.getId(),patient.getName(), patient.getPhone());
        }).toList();
    }

    public Patient getDetailsById(String id){
        return patientRepo.findById(id).orElseThrow(() -> new PatientNotFoundException("Id do paciente informado não possui registro"));
    }

    @Transactional
    public void saveAddressPatient(AddressOnPatientDTO dto, String patientId){
        Patient patient = getDetailsById(patientId);
        var address = new Address(dto, patient);
        addressRepo.save(address);
    }
}
