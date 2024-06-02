package com.psicomanager.api.controllers;

import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/patient")
public class PatientController {

    @PostMapping("/register")
    public void register(@RequestBody String data){
        System.out.println(data);
    }
}
