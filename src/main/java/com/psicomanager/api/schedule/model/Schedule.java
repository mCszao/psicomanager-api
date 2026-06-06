package com.psicomanager.api.schedule.model;

import com.psicomanager.api.patient.model.Patient;
import com.psicomanager.api.plan.model.Plan;
import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.StageEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "sessions_schedule")
@Table(name = "sessions_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "patient", nullable = false, referencedColumnName = "id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = true)
    private Plan plan;

    @Column(name = "DATE_START", nullable = false)
    private LocalDateTime dateStart;

    @Column(name = "DATE_END")
    private LocalDateTime dateEnd;

    @Column(name = "ANNOTATIONS")
    private String annotations;

    @Enumerated(EnumType.STRING)
    @Column(name = "STAGE")
    private StageEnum stage = StageEnum.OPENED;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private AttendanceTypeEnum type = AttendanceTypeEnum.PRESENTIAL;

    @OneToOne
    @JoinColumn(name = "RESCHEDULED_TO", nullable = true, referencedColumnName = "id")
    private Schedule rescheduledTo;

    /**
     * Valor cobrado por esta sessão.
     * Quando a sessão pertence a um plano, pode ser preenchido automaticamente
     * com o {@code pricePerSession} do plano.
     */
    @Column(name = "SESSION_VALUE", precision = 10, scale = 2)
    private BigDecimal sessionValue;

    /** Tenant: organização à qual esta sessão pertence. */
    @Column(name = "organization_id")
    private String organizationId;
}
