package com.psicomanager.api.domain.schedule;

import com.psicomanager.api.domain.patient.Patient;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity(name = "sessions_schedule")
@Table(name = "sessions_schedule")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @OneToOne()
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
