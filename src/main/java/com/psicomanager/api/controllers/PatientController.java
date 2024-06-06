package com.psicomanager.api.controllers;

import com.psicomanager.api.domain.address.AddressOnPatientDTO;
import com.psicomanager.api.domain.patient.Patient;
import com.psicomanager.api.domain.patient.PatientRegisterDTO;
import com.psicomanager.api.domain.patient.PatientResumeResponseDTO;
import com.psicomanager.api.dtos.BaseResponse;
import com.psicomanager.api.repositories.PatientRepository;
import com.psicomanager.api.services.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse> add(@RequestBody @Valid PatientRegisterDTO data){
            patientService.register(data);
            return ResponseEntity.ok(new BaseResponse(true, "Paciente salvo com sucesso!"));
    }

    @GetMapping()
    public ResponseEntity<BaseResponse> index(){
        var list = patientService.getAllPatientsComplete();
        return ResponseEntity.ok(new BaseResponse<>(true, list));
    }

    @GetMapping("/resume")
    public ResponseEntity<BaseResponse> indexResume(){
        var list = patientService.getAllPatientsResume();
        return ResponseEntity.ok(new BaseResponse<>(true, list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> detail(@PathVariable String id){
        var patient = patientService.getDetailsById(id);
        return ResponseEntity.ok(new BaseResponse<>(true, new PatientResumeResponseDTO(patient.getId(), patient.getName(), patient.getPhone())));
    }

    @PostMapping("/register/address")
    public ResponseEntity<BaseResponse> addAddress(@RequestParam(required = true) String patientId, @RequestBody @Valid AddressOnPatientDTO data){
        patientService.saveAddressPatient(data, patientId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Endereço adicionado com sucesso"));
    }
}
