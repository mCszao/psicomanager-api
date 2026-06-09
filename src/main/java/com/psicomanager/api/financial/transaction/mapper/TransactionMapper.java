package com.psicomanager.api.financial.transaction.mapper;

import com.psicomanager.api.financial.transaction.dto.TransactionResponseDTO;
import com.psicomanager.api.financial.transaction.model.FinancialTransaction;
import com.psicomanager.api.patient.dto.PatientResumeResponseDTO;
import com.psicomanager.api.patient.model.Patient;

/**
 * Converte entidades {@link FinancialTransaction} para DTOs de resposta.
 */
public class TransactionMapper {

    private TransactionMapper() {}

    public static TransactionResponseDTO toDto(FinancialTransaction t) {
        Patient patient = t.getPatientAccount().getPatient();
        PatientResumeResponseDTO patientResume = new PatientResumeResponseDTO(
                patient.getId(),
                patient.getName(),
                patient.getBirthdayDate(),
                patient.getEmail()
        );
        return new TransactionResponseDTO(
                t.getId(),
                t.getType(),
                t.getAmount(),
                t.getStatus(),
                t.getDueDate(),
                t.getPaidAt(),
                t.getPaymentMethod(),
                t.getNotes(),
                t.getCreatedAt(),
                patientResume,
                t.getPlan() != null ? t.getPlan().getId() : null,
                t.getSession() != null ? t.getSession().getId() : null,
                t.getSession() != null ? t.getSession().getDateStart() : null
        );
    }
}
