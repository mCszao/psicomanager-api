package com.psicomanager.api.domain.patient.mapper;

import com.psicomanager.api.domain.address.model.Address;
import com.psicomanager.api.domain.patient.dto.PatientRegisterDTO;
import com.psicomanager.api.domain.patient.dto.PatientResponseDTO;
import com.psicomanager.api.domain.patient.dto.PatientResumeResponseDTO;
import com.psicomanager.api.domain.patient.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {
    public Patient dtoToEntity(PatientRegisterDTO dto){
        var patient = new Patient();
        patient.setName(dto.name());
        patient.setEmail(dto.email());
        patient.setPhone(dto.phone());
        patient.setCpf(dto.cpf());
        patient.setBirthdayDate(dto.birthdayDate());
        if(dto.address() != null){
            Address transactAddress = new Address(dto.address(), patient);
            patient.addAddress(transactAddress);
        }
        return patient;
    }

    public static PatientResponseDTO toDto(Patient patient){
        return new PatientResponseDTO(patient.getId(),patient.getName(), patient.getEmail(), patient.getPhone(), patient.getCpf(), patient.getBirthdayDate(), patient.getAddresses());
    }

    public static PatientResumeResponseDTO toResumeDto(Patient patient){
        return new PatientResumeResponseDTO(patient.getId(), patient.getName(), patient.getBirthdayDate());
    }
}
