package com.psicomanager.api.financial.transaction.enums;

/**
 * Status possíveis de uma transação financeira.
 *
 * <p>Transições permitidas:</p>
 * <pre>
 * PENDING        → PAID | OVERDUE | PARTIALLY_PAID | CANCELLED
 * OVERDUE        → PAID | PARTIALLY_PAID | CANCELLED
 * PARTIALLY_PAID → PAID
 * ADVANCE        (estado terminal)
 * PAID           (estado terminal — só pode originar REFUND como nova linha)
 * CANCELLED      (estado terminal)
 * </pre>
 */
public enum TransactionStatusEnum {
    PENDING,
    PAID,
    OVERDUE,
    PARTIALLY_PAID,
    CANCELLED,
    ADVANCE
}
