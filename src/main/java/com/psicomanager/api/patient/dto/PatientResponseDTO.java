package com.psicomanager.api.patient.dto;

import com.psicomanager.api.patient.address.model.Address;
import com.psicomanager.api.document.dto.DocumentResponseDTO;
import com.psicomanager.api.schedule.dto.ScheduleResponseDTO;

import java.time.LocalDate;
import java.util.List;

public record PatientResponseDTO(
        String id,
        String name,
        String email,
        String phone,
        String cpf,
        LocalDate birthdayDate,
        List<Address> address,
        List<DocumentResponseDTO> documents,
        List<ScheduleResponseDTO> schedules
) {}
