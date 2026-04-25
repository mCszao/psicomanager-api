package com.psicomanager.api.alert;

import com.psicomanager.api.alert.dto.AlertRegisterDTO;
import com.psicomanager.api.alert.dto.AlertResponseDTO;
import com.psicomanager.api.core.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expõe os endpoints de gerenciamento de avisos.
 */
@RestController
@RequestMapping("/alerts")
@Slf4j
public class AlertController {

    // region Dependências

    @Autowired
    private AlertService alertService;

    // endregion

    // region Criação

    @PostMapping
    public ResponseEntity<BaseResponse<String>> create(@RequestBody @Valid AlertRegisterDTO body) {
        log.info("POST: /alerts");
        alertService.create(body);
        return ResponseEntity.ok(new BaseResponse<>(true, "Aviso criado com sucesso!"));
    }

    // endregion

    // region Consultas

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<BaseResponse<List<AlertResponseDTO>>> getByPatient(
            @PathVariable String patientId) {
        log.info("GET: /alerts/patient/" + patientId);
        return ResponseEntity.ok(new BaseResponse<>(true, alertService.getActiveByPatient(patientId)));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<BaseResponse<List<AlertResponseDTO>>> getBySession(
            @PathVariable String sessionId) {
        log.info("GET: /alerts/session/" + sessionId);
        return ResponseEntity.ok(new BaseResponse<>(true, alertService.getActiveBySession(sessionId)));
    }

    // endregion

    // region Mutações de estado

    @PatchMapping("/{id}/dismiss")
    public ResponseEntity<BaseResponse<String>> dismiss(@PathVariable String id) {
        log.info("PATCH: /alerts/" + id + "/dismiss");
        alertService.dismiss(id);
        return ResponseEntity.ok(new BaseResponse<>(true, "Aviso descartado com sucesso!"));
    }

    // endregion
}
