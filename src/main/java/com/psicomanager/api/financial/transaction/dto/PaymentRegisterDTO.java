package com.psicomanager.api.financial.transaction.dto;

import com.psicomanager.api.financial.transaction.enums.PaymentMethodEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRegisterDTO(
        @NotNull String transactionId,
        @NotNull @Positive BigDecimal amountPaid,
        @NotNull PaymentMethodEnum paymentMethod,
        String notes
) {}
