package com.psicomanager.api.plan.template.model;

import com.psicomanager.api.schedule.enums.FrequencyEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity(name = "plan_templates")
@Table(name = "plan_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PlanTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

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

    @Column(name = "TOTAL_VALUE", precision = 10, scale = 2)
    private BigDecimal totalValue;

    @PrePersist
    @PreUpdate
    public void calculateTotalValue() {
        if (pricePerSession != null && sessionsCount != null) {
            this.totalValue = pricePerSession.multiply(BigDecimal.valueOf(sessionsCount));
        }
    }
}
