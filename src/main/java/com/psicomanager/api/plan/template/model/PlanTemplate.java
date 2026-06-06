package com.psicomanager.api.plan.template.model;

import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Template reutilizável de plano de atendimento.
 * <p>
 * Define um modelo de plano com frequência, preço, quantidade de sessões e tipo
 * de atendimento que pode ser aplicado a múltiplos pacientes. Ao aplicar um template,
 * todos os valores são copiados para o {@link com.psicomanager.api.plan.model.Plan}
 * e podem ser ajustados individualmente.
 * </p>
 */
@Entity(name = "plan_templates")
@Table(name = "plan_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PlanTemplate {

    // region Identidade

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // endregion

    // region Dados do template

    @NotBlank
    @Column(name = "TITLE", nullable = false, length = 255)
    private String title;

    @NotNull
    @Column(name = "PRICE_PER_SESSION", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerSession;

    @NotNull
    @Min(1)
    @Column(name = "SESSIONS_COUNT", nullable = false)
    private Integer sessionsCount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "FREQUENCY", nullable = false)
    private FrequencyEnum frequency;

    /**
     * Calculado automaticamente via {@link #calculateTotalValue()} antes de
     * qualquer persistência ou atualização.
     */
    @Column(name = "TOTAL_VALUE", precision = 10, scale = 2)
    private BigDecimal totalValue;

    /**
     * Tipo de atendimento padrão para sessões geradas a partir deste template.
     * Quando presente, é herdado pelo plano criado a partir do template e pode
     * ser sobrescrito individualmente por paciente.
     * Nullable para compatibilidade com templates criados antes desta funcionalidade.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ATTENDANCE_TYPE")
    private AttendanceTypeEnum attendanceType;

    /** Tenant: organização à qual este template pertence. */
    @Column(name = "organization_id")
    private String organizationId;

    // endregion

    // region Hooks JPA

    /**
     * Recalcula o valor total do template antes de qualquer persistência ou atualização.
     */
    @PrePersist
    @PreUpdate
    public void calculateTotalValue() {
        if (pricePerSession != null && sessionsCount != null) {
            this.totalValue = pricePerSession.multiply(BigDecimal.valueOf(sessionsCount));
        }
    }

    // endregion
}
