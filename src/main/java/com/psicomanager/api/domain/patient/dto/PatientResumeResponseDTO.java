package com.psicomanager.api.domain.patient.dto;

import java.time.LocalDate;

public record PatientResumeResponseDTO(
        String id,
        String name,
        LocalDate birthdayDate
) {

}
