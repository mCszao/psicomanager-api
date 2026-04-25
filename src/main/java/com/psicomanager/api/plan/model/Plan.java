package com.psicomanager.api.plan.model;

import com.psicomanager.api.patient.model.Patient;
import com.psicomanager.api.plan.template.model.PlanTemplate;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "plans")
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "plan_template_id", nullable = true)
    private PlanTemplate planTemplate;

    @Column(name = "TITLE", length = 255)
    private String title;

    // Pode sobrescrever o valor do template por paciente
    @Column(name = "PRICE_PER_SESSION", precision = 10, scale = 2)
    private BigDecimal pricePerSession;

    @Column(name = "SESSIONS_COUNT")
    private Integer sessionsCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "FREQUENCY")
    private FrequencyEnum frequency;

    @Column(name = "TOTAL_VALUE", precision = 10, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "ADHERENCE_DATE", nullable = false)
    private LocalDate adherenceDate;

    // Calculado a partir de frequency + sessionsCount
    @Column(name = "ESTIMATED_END_DATE")
    private LocalDate estimatedEndDate;

    // Preenchido automaticamente na conclusão da primeira sessão do plano
    @Column(name = "STARTED_AT")
    private LocalDateTime startedAt;

    // Preenchido automaticamente na conclusão da última sessão do plano
    @Column(name = "ENDED_AT")
    private LocalDateTime endedAt;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    @PreUpdate
    public void calculateTotalValue() {
        if (pricePerSession != null && sessionsCount != null) {
            this.totalValue = pricePerSession.multiply(BigDecimal.valueOf(sessionsCount));
        }
    }
}
