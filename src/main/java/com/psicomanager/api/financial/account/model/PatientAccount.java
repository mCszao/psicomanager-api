package com.psicomanager.api.financial.account.model;

import com.psicomanager.api.patient.model.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Conta financeira vinculada a um paciente.
 *
 * <p>Funciona como um espelho de saldo derivado — os valores são calculados
 * a partir das transações do ledger ({@code financial_transactions}) e
 * atualizados a cada mudança de estado de transação.</p>
 *
 * <ul>
 *   <li>{@code balance} — total de cobranças em aberto (PENDING + OVERDUE)</li>
 *   <li>{@code creditBalance} — adiantamentos disponíveis para abater futuras cobranças</li>
 * </ul>
 */
@Entity(name = "patient_accounts")
@Table(name = "patient_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PatientAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    @Column(name = "balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "credit_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal creditBalance = BigDecimal.ZERO;

    /** Tenant: organização à qual esta conta de paciente pertence. */
    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
