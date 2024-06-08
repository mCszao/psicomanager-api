package com.psicomanager.api.controllers;

import com.psicomanager.api.domain.address.AddressOnPatientDTO;
import com.psicomanager.api.domain.patient.Patient;
import com.psicomanager.api.domain.patient.PatientRegisterDTO;
import com.psicomanager.api.domain.patient.PatientResponseDTO;
import com.psicomanager.api.domain.patient.PatientResumeResponseDTO;
import com.psicomanager.api.dtos.BaseResponse;
import com.psicomanager.api.repositories.PatientRepository;
import com.psicomanager.api.services.PatientService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/patients")
@Slf4j
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<String>> register(@RequestBody @Valid PatientRegisterDTO data){
            log.info("POST: /patients/register");
            patientService.register(data);
            return ResponseEntity.ok(new BaseResponse<>(true, "Paciente salvo com sucesso!"));
    }

    @GetMapping()
    public ResponseEntity<BaseResponse<List<PatientResponseDTO>>> index(){
        log.info("GET: /patients/");
        var list = patientService.getAllPatientsComplete();
        return ResponseEntity.ok(new BaseResponse<>(true, list));
    }

    @GetMapping("/resume")
    public ResponseEntity<BaseResponse<List<PatientResumeResponseDTO>>> indexResume(){
        log.info("GET: /patients/resume");
        var list = patientService.getAllPatientsResume();
        return ResponseEntity.ok(new BaseResponse<>(true, list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<PatientResponseDTO>> detail(@PathVariable String id){
        log.info("GET: /patients/" + id);
        var patient = patientService.getDetailsById(id);
        return ResponseEntity.ok(new BaseResponse<>(true, patient));
    }

    @PostMapping("/register/address")
    public ResponseEntity<BaseResponse<String>> addAddress(@RequestParam(required = true) String patientId, @RequestBody @Valid AddressOnPatientDTO data){
        log.info("POST: /patients/register/address?patientId="+patientId);
        patientService.saveAddressPatient(data, patientId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Endereço adicionado com sucesso"));
    }
}
