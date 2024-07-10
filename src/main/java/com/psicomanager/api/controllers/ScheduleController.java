package com.psicomanager.api.controllers;

import com.psicomanager.api.domain.schedule.dto.ScheduleRegisterDTO;
import com.psicomanager.api.domain.schedule.dto.ScheduleResponseDTO;
import com.psicomanager.api.core.dto.BaseResponse;
import com.psicomanager.api.services.ScheduleService;
import com.psicomanager.api.services.SortService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedules")
@Slf4j
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private SortService sortService;
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<String>> register(@RequestBody @Valid ScheduleRegisterDTO data){
        log.info("POST: /schedules/register");
        scheduleService.createSchedule(data);
        return ResponseEntity.ok(new BaseResponse<>(true,"Agendamento realizado com sucesso!"));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<ScheduleResponseDTO>>> index(@RequestParam(defaultValue = "asc") String order){
        log.info("GET: /schedules/");
        var schedules = sortService.sortScheduleDates(order);
        return ResponseEntity.ok(new BaseResponse<>(true, schedules));
    }

    @GetMapping("/patient")
    public ResponseEntity<BaseResponse<List<ScheduleResponseDTO>>> detailsByPatient(@RequestParam(required = true) String id){
        log.info("GET: /schedules/patient?id="+id);
        var schedules = scheduleService.getAllByPatientId(id);
        return ResponseEntity.ok(new BaseResponse<>(true, schedules));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ScheduleResponseDTO>> details(@PathVariable String id){
        log.info("GET: /schedules/"+id);
        var schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(new BaseResponse<>(true, schedule));
    }
}
