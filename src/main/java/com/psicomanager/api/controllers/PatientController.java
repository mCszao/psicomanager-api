package com.psicomanager.api.controllers;

import com.psicomanager.api.domain.patient.Patient;
import com.psicomanager.api.domain.patient.PatientRegisterDTO;
import com.psicomanager.api.dtos.BaseResponse;
import com.psicomanager.api.repositories.PatientRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientRepository patientRepo;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse> register(@RequestBody @Valid PatientRegisterDTO data){
        try {
            patientRepo.save(new Patient(data));
            return ResponseEntity.ok(new BaseResponse(true, "Paciente salvo com sucesso!"));
        }catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new BaseResponse(false, ex.getMessage()));
        }
    }
}
