package com.psicomanager.api.financial.transaction.model;

import com.psicomanager.api.financial.account.model.PatientAccount;
import com.psicomanager.api.financial.account.model.PsychologistAccount;
import com.psicomanager.api.financial.transaction.enums.PaymentMethodEnum;
import com.psicomanager.api.financial.transaction.enums.TransactionStatusEnum;
import com.psicomanager.api.financial.transaction.enums.TransactionTypeEnum;
import com.psicomanager.api.plan.model.Plan;
import com.psicomanager.api.schedule.model.Schedule;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro imutável de um evento financeiro no ledger.
 *
 * <p>Registros nunca são deletados do ledger. Cancelamentos alteram o {@code status}
 * para CANCELLED ou geram uma nova transação do tipo REFUND. O campo {@code amount}
 * é sempre positivo — o {@code type} define a direção semântica do fluxo.</p>
 */
@Entity(name = "financial_transactions")
@Table(name = "financial_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class FinancialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "patient_account_id", nullable = false)
    private PatientAccount patientAccount;

    @ManyToOne
    @JoinColumn(name = "psychologist_account_id", nullable = false)
    private PsychologistAccount psychologistAccount;

    /** Preenchido apenas para transações do tipo PLAN_CHARGE. */
    @ManyToOne(optional = true)
    @JoinColumn(name = "plan_id", nullable = true)
    private Plan plan;

    /** Preenchido apenas para transações do tipo SESSION_CHARGE. */
    @ManyToOne(optional = true)
    @JoinColumn(name = "session_id", nullable = true)
    private Schedule session;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionTypeEnum type;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** Parte do {@code amount} quitada em dinheiro (pagamentos via /financial/payments). */
    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    /** Parte do {@code amount} quitada com crédito de adiantamento (liquidação automática). */
    @Column(name = "credit_applied", nullable = false, precision = 12, scale = 2)
    private BigDecimal creditApplied = BigDecimal.ZERO;

    /** Data de vencimento. Nulo para pagamentos e adiantamentos. */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /** Preenchido quando status muda para PAID. */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /** Preenchido apenas quando há pagamento efetivo. */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethodEnum paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatusEnum status = TransactionStatusEnum.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Tenant: organização à qual esta transação pertence. */
    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Valor ainda em aberto desta transação: {@code amount - amountPaid - creditApplied},
     * nunca negativo. Para cobranças quitadas resulta em zero; para adiantamentos não é
     * semanticamente relevante.
     */
    @Transient
    public BigDecimal getOutstanding() {
        BigDecimal total = amount != null ? amount : BigDecimal.ZERO;
        BigDecimal paid = amountPaid != null ? amountPaid : BigDecimal.ZERO;
        BigDecimal credit = creditApplied != null ? creditApplied : BigDecimal.ZERO;
        return total.subtract(paid).subtract(credit).max(BigDecimal.ZERO);
    }
}
