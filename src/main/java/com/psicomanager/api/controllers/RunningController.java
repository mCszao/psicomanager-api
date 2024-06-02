package com.psicomanager.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RunningController {

    @GetMapping("/running")
    public String isRunning(){
        return "Appications is running correctly..";
    }

}
