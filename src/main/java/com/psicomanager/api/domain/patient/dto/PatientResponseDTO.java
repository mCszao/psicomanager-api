package com.psicomanager.api.domain.patient.dto;

import com.psicomanager.api.domain.address.model.Address;
import com.psicomanager.api.domain.document.dto.DocumentResponseDTO;
import com.psicomanager.api.domain.document.model.Document;
import com.psicomanager.api.domain.patient.model.Patient;

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
        List<Address> address,
        List<DocumentResponseDTO> documents
) {
}
