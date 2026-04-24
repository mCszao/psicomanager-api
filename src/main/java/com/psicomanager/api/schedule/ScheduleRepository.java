package com.psicomanager.api.schedule;

import com.psicomanager.api.schedule.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, String> {

    Schedule findByDateStart(LocalDateTime dateStart);

    List<Schedule> findByPatientId(String patientId);

    /**
     * Retorna sessões abertas que conflitam com o intervalo [newStart, newEnd).
     *
     * Lógica de sobreposição: A sobrepõe B quando A.start < B.end E A.end > B.start.
     * Como dateEnd pode ser nulo, tratamos sessão sem fim como se durasse 1h a partir de dateStart.
     * O parâmetro excludeId permite ignorar a própria sessão no caso de reagendamento (passe null para ignorar).
     */
    @Query("""
        SELECT s FROM sessions_schedule s
        WHERE s.stage = 'OPENED'
          AND (:excludeId IS NULL OR s.id <> :excludeId)
          AND s.dateStart < :newEnd
          AND COALESCE(s.dateEnd, s.dateStart + 1 HOUR) > :newStart
    """)
    List<Schedule> findConflictingSchedules(
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("excludeId") String excludeId
    );
}
