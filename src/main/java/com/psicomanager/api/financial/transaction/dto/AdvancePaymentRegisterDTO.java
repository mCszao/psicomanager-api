package com.psicomanager.api.financial.transaction.dto;

import com.psicomanager.api.financial.transaction.enums.PaymentMethodEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AdvancePaymentRegisterDTO(
        @NotBlank String patientId,
        @NotNull @Positive BigDecimal amount,
        @NotNull PaymentMethodEnum paymentMethod,
        String notes
) {}
