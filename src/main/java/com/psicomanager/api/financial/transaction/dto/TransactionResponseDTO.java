package com.psicomanager.api.financial.transaction.dto;

import com.psicomanager.api.financial.transaction.enums.PaymentMethodEnum;
import com.psicomanager.api.financial.transaction.enums.TransactionStatusEnum;
import com.psicomanager.api.financial.transaction.enums.TransactionTypeEnum;
import com.psicomanager.api.patient.dto.PatientResumeResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponseDTO(
        String id,
        TransactionTypeEnum type,
        BigDecimal amount,
        TransactionStatusEnum status,
        LocalDate dueDate,
        LocalDateTime paidAt,
        PaymentMethodEnum paymentMethod,
        String notes,
        LocalDateTime createdAt,
        PatientResumeResponseDTO patient,
        String planId,
        String sessionId,
        LocalDateTime sessionDate
) {}
