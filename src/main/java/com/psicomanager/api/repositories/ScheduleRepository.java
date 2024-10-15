package com.psicomanager.api.repositories;

import com.psicomanager.api.repositories.schedule.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, String> {
    Schedule findByDateStart(LocalDateTime dateStart);

    List<Schedule> findByPatientId(String patientId);
    @Query("SELECT s FROM sessions_schedule s WHERE s.dateEnd BETWEEN :dateStart AND :dateEnd")
    List<Schedule> getScheduleBetweenStartEnd(@Param("dateStart") LocalDateTime dateStart, @Param("dateEnd") LocalDateTime dateEnd);
}
