package com.psicomanager.api.repositories.schedule.model;

import com.psicomanager.api.repositories.patient.model.Patient;
import com.psicomanager.api.domain.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.domain.schedule.enums.StageEnum;
import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "STAGE")
    private StageEnum stage = StageEnum.OPENED;
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private AttendanceTypeEnum type = AttendanceTypeEnum.PRESENTIAL;
    @OneToOne
    @JoinColumn(name = "RESCHEDULED_TO", nullable = true, referencedColumnName = "id")
    private Schedule rescheduledTo;
}
