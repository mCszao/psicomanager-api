package com.psicomanager.api.plan;

import com.psicomanager.api.core.dto.BaseResponse;
import com.psicomanager.api.plan.dto.PlanRegisterDTO;
import com.psicomanager.api.plan.dto.PlanResponseDTO;
import com.psicomanager.api.plan.template.dto.PlanTemplateRegisterDTO;
import com.psicomanager.api.plan.template.dto.PlanTemplateResponseDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expõe os endpoints de gerenciamento de templates e planos de atendimento.
 */
@RestController
@RequestMapping("/plans")
@Slf4j
public class PlanController {

    // region Dependências

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanTemplateService planTemplateService;

    // endregion

    // region Templates

    @PostMapping("/templates")
    public ResponseEntity<BaseResponse<String>> createTemplate(@RequestBody @Valid PlanTemplateRegisterDTO body) {
        log.info("POST: /plans/templates");
        planTemplateService.create(body);
        return ResponseEntity.ok(new BaseResponse<>(true, "Template criado com sucesso!"));
    }

    @GetMapping("/templates")
    public ResponseEntity<BaseResponse<List<PlanTemplateResponseDTO>>> listTemplates() {
        log.info("GET: /plans/templates");
        return ResponseEntity.ok(new BaseResponse<>(true, planTemplateService.getAll()));
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<BaseResponse<PlanTemplateResponseDTO>> getTemplate(@PathVariable String id) {
        log.info("GET: /plans/templates/" + id);
        return ResponseEntity.ok(new BaseResponse<>(true, planTemplateService.getById(id)));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<BaseResponse<String>> deleteTemplate(@PathVariable String id) {
        log.info("DELETE: /plans/templates/" + id);
        planTemplateService.delete(id);
        return ResponseEntity.ok(new BaseResponse<>(true, "Template removido com sucesso!"));
    }

    // endregion

    // region Planos

    @PostMapping
    public ResponseEntity<BaseResponse<String>> createPlan(@RequestBody @Valid PlanRegisterDTO body) {
        log.info("POST: /plans");
        planService.createPlan(body);
        return ResponseEntity.ok(new BaseResponse<>(true, "Plano criado com sucesso!"));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<BaseResponse<List<PlanResponseDTO>>> listByPatient(@PathVariable String patientId) {
        log.info("GET: /plans/patient/" + patientId);
        return ResponseEntity.ok(new BaseResponse<>(true, planService.getAllByPatient(patientId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<PlanResponseDTO>> getById(@PathVariable String id) {
        log.info("GET: /plans/" + id);
        return ResponseEntity.ok(new BaseResponse<>(true, planService.getById(id)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<BaseResponse<String>> deactivate(@PathVariable String id) {
        log.info("PATCH: /plans/" + id + "/deactivate");
        planService.deactivatePlan(id);
        return ResponseEntity.ok(new BaseResponse<>(true, "Plano desativado com sucesso!"));
    }

    // endregion
}
