package com.psicomanager.api.patient.mapper;

import com.psicomanager.api.patient.address.model.Address;
import com.psicomanager.api.document.mapper.DocumentMapper;
import com.psicomanager.api.patient.dto.PatientRegisterDTO;
import com.psicomanager.api.patient.dto.PatientResponseDTO;
import com.psicomanager.api.patient.dto.PatientResumeResponseDTO;
import com.psicomanager.api.patient.model.Patient;
import com.psicomanager.api.schedule.mapper.ScheduleMapper;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public Patient dtoToEntity(PatientRegisterDTO dto) {
        var patient = new Patient();
        patient.setName(dto.name());
        patient.setEmail(dto.email());
        patient.setPhone(dto.phone());
        patient.setCpf(dto.cpf());
        patient.setBirthdayDate(dto.birthdayDate());
        if (dto.address() != null) {
            Address transactAddress = new Address(dto.address(), patient);
            patient.addAddress(transactAddress);
        }
        return patient;
    }

    public static PatientResponseDTO toDto(Patient patient) {
        var documents = patient.getDocuments().stream().map(DocumentMapper::documentToDto).toList();
        var schedules = patient.getSchedules().stream().map(ScheduleMapper::toDto).toList();
        return new PatientResponseDTO(
                patient.getId(),
                patient.getName(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getCpf(),
                patient.getBirthdayDate(),
                patient.getAddresses(),
                documents,
                schedules
        );
    }

    public static PatientResumeResponseDTO toResumeDto(Patient patient) {
        return new PatientResumeResponseDTO(
                patient.getId(),
                patient.getName(),
                patient.getBirthdayDate(),
                patient.getEmail()
        );
    }
}
