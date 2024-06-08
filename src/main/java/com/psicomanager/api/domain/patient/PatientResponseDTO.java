package com.psicomanager.api.domain.patient;

import com.psicomanager.api.domain.address.Address;

import java.time.LocalDate;
import java.util.List;

public record PatientResponseDTO(
        String id,
        String name,
        String email,
        String phone,
        String cpf,
        LocalDate
        birthdayDate,
        List<Address> address
) {

    public static PatientResponseDTO of(Patient patient){
        return new PatientResponseDTO(patient.getId(),patient.getName(), patient.getEmail(), patient.getPhone(), patient.getCpf(), patient.getBirthdayDate(), patient.getAddresses());
    }
}
