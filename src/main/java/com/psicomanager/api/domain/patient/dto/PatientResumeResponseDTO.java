package com.psicomanager.api.domain.patient.dto;

import com.psicomanager.api.domain.patient.model.Patient;

public record PatientResumeResponseDTO(
        String id,
        String name,
        String phone
) {

}
