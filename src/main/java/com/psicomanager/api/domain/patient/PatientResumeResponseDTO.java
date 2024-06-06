package com.psicomanager.api.domain.patient;

public record PatientResumeResponseDTO(
        String id,
        String name,
        String phone
) {
    public static PatientResumeResponseDTO of(Patient patient){
        return new PatientResumeResponseDTO(patient.getId(), patient.getName(), patient.getPhone());
    }
}
