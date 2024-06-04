package com.psicomanager.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @PostMapping("/register")
    public ResponseEntity<String> regiter(@RequestBody Object data){
        return ResponseEntity.ok("Schedule running..." + data);
    }
}
