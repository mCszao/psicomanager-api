package com.psicomanager.api.financial.transaction.dto;

import java.math.BigDecimal;

public record FinancialSummaryDTO(
        BigDecimal totalReceivable,
        BigDecimal totalReceived,
        BigDecimal totalOverdue,
        Long totalPendingCount
) {}
