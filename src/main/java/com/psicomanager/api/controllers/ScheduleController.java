package com.psicomanager.api.controllers;

import com.psicomanager.api.domain.schedule.ScheduleRegisterDTO;
import com.psicomanager.api.dtos.BaseResponse;
import com.psicomanager.api.services.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService service;
    @PostMapping("/register")
    public ResponseEntity<BaseResponse> register(@RequestBody ScheduleRegisterDTO data){
        service.createSchedule(data);
        return ResponseEntity.ok(new BaseResponse(true,"Agendamento realizado com sucesso!"));
    }

    @GetMapping
    public ResponseEntity<BaseResponse> index(){
        var schedules = service.getAllSchedules();
        return ResponseEntity.ok(new BaseResponse(true, schedules));
    }

    @GetMapping("/patient")
    public ResponseEntity<BaseResponse> detailsByPatient(@RequestParam(required = true) String id){
        var schedules = service.getAllByPatientId(id);
        return ResponseEntity.ok(new BaseResponse(true, schedules));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> details(@PathVariable String id){
        var schedule = service.getScheduleById(id);
        return ResponseEntity.ok(new BaseResponse(true, schedule));
    }
}
