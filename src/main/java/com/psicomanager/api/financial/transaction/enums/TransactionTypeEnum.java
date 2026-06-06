package com.psicomanager.api.financial.transaction.enums;

/**
 * Tipos de transação financeira suportados pelo ledger.
 *
 * <ul>
 *   <li>{@link #SESSION_CHARGE} – cobrança gerada automaticamente ao concluir uma sessão avulsa ou de plano contínuo</li>
 *   <li>{@link #PLAN_CHARGE} – cobrança gerada automaticamente ao criar um plano fechado (IS_CONTINUOUS = false)</li>
 *   <li>{@link #ADVANCE_PAYMENT} – adiantamento registrado manualmente pelo psicólogo</li>
 *   <li>{@link #PAYMENT} – pagamento manual de uma cobrança existente</li>
 *   <li>{@link #CREDIT_ADJUSTMENT} – ajuste manual de crédito pelo psicólogo</li>
 *   <li>{@link #REFUND} – estorno gerado ao cancelar uma transação já paga</li>
 * </ul>
 */
public enum TransactionTypeEnum {
    SESSION_CHARGE,
    PLAN_CHARGE,
    ADVANCE_PAYMENT,
    PAYMENT,
    CREDIT_ADJUSTMENT,
    REFUND
}
