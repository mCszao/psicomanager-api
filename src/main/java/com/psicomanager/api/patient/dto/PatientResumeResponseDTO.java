package com.psicomanager.api.patient.dto;

import java.time.LocalDate;

public record PatientResumeResponseDTO(
        String id,
        String name,
        LocalDate birthdayDate,
        String email
) {}
