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
        try {
            if(service.save(data)) return ResponseEntity.ok("Agendamento realizado com sucesso!");
            return ResponseEntity.badRequest().body("Não foi possível realizar o agendamento");
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
