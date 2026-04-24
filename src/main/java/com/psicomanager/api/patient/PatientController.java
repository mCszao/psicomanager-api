package com.psicomanager.api.patient;

import com.psicomanager.api.core.dto.BaseResponse;
import com.psicomanager.api.patient.address.AddressPatientService;
import com.psicomanager.api.patient.address.dto.AddressOnPatientDTO;
import com.psicomanager.api.patient.dto.PatientRegisterDTO;
import com.psicomanager.api.patient.dto.PatientResponseDTO;
import com.psicomanager.api.patient.dto.PatientResumeResponseDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@Slf4j
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AddressPatientService addressPatientService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<String>> register(@RequestBody @Valid PatientRegisterDTO data) {
        log.info("POST: /patients/register");
        patientService.register(data);
        return ResponseEntity.ok(new BaseResponse<>(true, "Paciente salvo com sucesso!"));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<PatientResponseDTO>>> index() {
        log.info("GET: /patients/");
        var list = patientService.getAllPatientsComplete();
        return ResponseEntity.ok(new BaseResponse<>(true, list));
    }

    @GetMapping("/resume")
    public ResponseEntity<BaseResponse<List<PatientResumeResponseDTO>>> indexResume() {
        log.info("GET: /patients/resume");
        var list = patientService.getAllPatientsResume();
        return ResponseEntity.ok(new BaseResponse<>(true, list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<PatientResponseDTO>> detail(@PathVariable String id) {
        log.info("GET: /patients/" + id);
        var patient = patientService.getDetailsById(id);
        return ResponseEntity.ok(new BaseResponse<>(true, patient));
    }

    @PostMapping("/register/address")
    public ResponseEntity<BaseResponse<String>> addAddress(
            @RequestParam(required = true) String patientId,
            @RequestBody @Valid AddressOnPatientDTO data) {
        log.info("POST: /patients/register/address?patientId=" + patientId);
        addressPatientService.saveAddressPatient(data, patientId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Endereço adicionado com sucesso"));
    }
}
