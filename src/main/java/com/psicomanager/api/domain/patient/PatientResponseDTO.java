package com.psicomanager.api.domain.patient;

import com.psicomanager.api.domain.address.Address;
import com.psicomanager.api.domain.address.AddressOnPatientDTO;

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
}
