package com.psicomanager.api.controllers;

import com.psicomanager.api.domain.schedule.ScheduleRegisterDTO;
import com.psicomanager.api.services.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService service;
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody ScheduleRegisterDTO data){
        service.createSchedule(data);
        return ResponseEntity.ok("Agendamento realizado com sucesso!");
    }
}
