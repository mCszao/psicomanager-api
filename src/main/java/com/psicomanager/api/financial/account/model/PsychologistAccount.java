package com.psicomanager.api.financial.account.model;

import com.psicomanager.api.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Conta financeira vinculada ao psicólogo (usuário do sistema).
 *
 * <p>Funciona como um espelho de saldo derivado — os valores são calculados
 * a partir das transações do ledger ({@code financial_transactions}) e
 * atualizados a cada mudança de estado de transação.</p>
 *
 * <ul>
 *   <li>{@code totalReceivable} — soma de transações PENDING e OVERDUE</li>
 *   <li>{@code totalReceived} — soma de transações PAID</li>
 * </ul>
 */
@Entity(name = "psychologist_accounts")
@Table(name = "psychologist_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PsychologistAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_receivable", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalReceivable = BigDecimal.ZERO;

    @Column(name = "total_received", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalReceived = BigDecimal.ZERO;

    /** Tenant: organização à qual esta conta de psicólogo pertence. */
    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
