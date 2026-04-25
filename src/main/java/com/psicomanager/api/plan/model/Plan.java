package com.psicomanager.api.plan.model;

import com.psicomanager.api.patient.model.Patient;
import com.psicomanager.api.plan.template.model.PlanTemplate;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa um plano de atendimento vinculado a um paciente específico.
 * <p>
 * Um plano pode ser criado livremente ou a partir de um {@link PlanTemplate}.
 * Quando criado a partir de um template, os valores são herdados mas podem ser
 * sobrescritos individualmente por paciente (ex: desconto social).
 * </p>
 * <p>
 * O ciclo de vida do plano é controlado por {@code isActive}, {@code startedAt}
 * e {@code endedAt}, que são preenchidos automaticamente conforme as sessões
 * vinculadas são concluídas.
 * </p>
 */
@Entity(name = "plans")
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Plan {

    // region Identidade

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // endregion

    // region Relacionamentos

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /**
     * Template de origem, se o plano foi criado a partir de um.
     * Pode ser nulo para planos criados livremente.
     */
    @ManyToOne
    @JoinColumn(name = "plan_template_id", nullable = true)
    private PlanTemplate planTemplate;

    // endregion

    // region Dados do plano

    @Column(name = "TITLE", length = 255)
    private String title;

    /**
     * Valor por sessão. Pode sobrescrever o valor definido no template
     * para acomodar ajustes individuais por paciente.
     */
    @Column(name = "PRICE_PER_SESSION", precision = 10, scale = 2)
    private BigDecimal pricePerSession;

    @Column(name = "SESSIONS_COUNT")
    private Integer sessionsCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "FREQUENCY")
    private FrequencyEnum frequency;

    /**
     * Calculado automaticamente via {@link #calculateTotalValue()} antes de
     * qualquer persistência ou atualização.
     */
    @Column(name = "TOTAL_VALUE", precision = 10, scale = 2)
    private BigDecimal totalValue;

    // endregion

    // region Datas

    @Column(name = "ADHERENCE_DATE", nullable = false)
    private LocalDate adherenceDate;

    /**
     * Data estimada de encerramento, calculada a partir de {@code frequency}
     * e {@code sessionsCount} quando não informada manualmente.
     */
    @Column(name = "ESTIMATED_END_DATE")
    private LocalDate estimatedEndDate;

    /**
     * Preenchido automaticamente na conclusão da primeira sessão do plano.
     */
    @Column(name = "STARTED_AT")
    private LocalDateTime startedAt;

    /**
     * Preenchido automaticamente na conclusão da última sessão do plano.
     */
    @Column(name = "ENDED_AT")
    private LocalDateTime endedAt;

    // endregion

    // region Status

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    /**
     * Indica se o plano é de acompanhamento contínuo, sem fim definido.
     * <p>
     * Quando {@code true}, o plano não encerra automaticamente ao concluir sessões
     * e o campo {@code sessionsCount} é usado apenas como referência de geração,
     * não como critério de encerramento.
     * </p>
     */
    @Column(name = "IS_CONTINUOUS", nullable = false)
    private Boolean isContinuous = true;

    // endregion

    // region Hooks JPA

    /**
     * Recalcula o valor total do plano antes de qualquer persistência ou atualização.
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
