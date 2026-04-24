package com.psicomanager.api.schedule;

import com.psicomanager.api.schedule.dto.ScheduleResponseDTO;
import com.psicomanager.api.schedule.mapper.ScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SortService {

    @Autowired
    private ScheduleService scheduleService;

    public List<ScheduleResponseDTO> sortScheduleDates(String order) {
        var schedules = new ArrayList<>(scheduleService.getAllSchedules());
        if ("desc".equalsIgnoreCase(order)) {
            schedules.sort((d1, d2) -> d2.dateStart().compareTo(d1.dateStart()));
        } else {
            schedules.sort((d1, d2) -> d1.dateStart().compareTo(d2.dateStart()));
        }
        return schedules;
    }
}
