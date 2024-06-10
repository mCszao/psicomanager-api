package com.psicomanager.api.domain.schedule.model;

import com.psicomanager.api.domain.patient.model.Patient;
import com.psicomanager.api.domain.schedule.dto.ScheduleRegisterDTO;
import com.psicomanager.api.domain.schedule.enums.StageEnum;
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
    @ManyToOne()
    @JoinColumn(name = "patient", nullable = false, referencedColumnName = "id")
    private Patient patient;
    @Column(name = "DATE_START", nullable = false)
    private LocalDateTime dateStart;
    @Column(name = "DATE_END", nullable = true)
    private LocalDateTime dateEnd;
    @Column(name = "ANNOTATIONS", nullable = true)
    private String annotations;
    @Enumerated(EnumType.STRING)
    private StageEnum stage = StageEnum.OPENED;


}
