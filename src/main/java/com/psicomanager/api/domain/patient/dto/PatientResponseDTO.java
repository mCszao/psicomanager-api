package com.psicomanager.api.domain.patient.dto;

import com.psicomanager.api.repositories.address.model.Address;
import com.psicomanager.api.domain.document.dto.DocumentResponseDTO;
import com.psicomanager.api.domain.schedule.dto.ScheduleResponseDTO;

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
        List<DocumentResponseDTO> documents,
        List<ScheduleResponseDTO> schedules
) {
}
