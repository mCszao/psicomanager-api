package com.psicomanager.api.infra.config;

import com.psicomanager.api.auth.exception.InvalidRefreshTokenException;
import com.psicomanager.api.plan.exception.InvalidPlanConfigException;
import com.psicomanager.api.plan.exception.PlanNotFoundException;
import com.psicomanager.api.plan.exception.PlanTemplateNotFoundException;
import com.psicomanager.api.core.dto.BaseResponse;
import com.psicomanager.api.core.exception.DuplicateEntryException;
import com.psicomanager.api.document.exception.ContractWithoutArgsException;
import com.psicomanager.api.document.exception.DocumentNotFoundException;
import com.psicomanager.api.patient.exception.PatientNotFoundException;
import com.psicomanager.api.schedule.exception.ScheduleConflictTimeException;
import com.psicomanager.api.schedule.exception.ScheduleNotFoundException;
import com.psicomanager.api.user.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

    @ExceptionHandler(InvalidRefreshTokenException.class)
    private ResponseEntity<BaseResponse<String>> invalidRefreshTokenHandler(InvalidRefreshTokenException ex) {
        log.error("Refresh token inválido ou expirado");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    private ResponseEntity<BaseResponse<String>> userNotFoundHandler(UserNotFoundException ex) {
        log.error("Usuário informado não foi encontrado");
        return ResponseEntity.internalServerError().body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEntryException.class)
    private ResponseEntity<BaseResponse<String>> duplicateUserEntryHandler(DuplicateEntryException ex) {
        log.error("Informações do usuário enviado já possuem registro no banco de dados");
        return ResponseEntity.badRequest().body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(PatientNotFoundException.class)
    private ResponseEntity<BaseResponse<String>> patientNotFoundHandler(PatientNotFoundException ex) {
        log.error("Paciente informado não foi encontrado");
        return ResponseEntity.internalServerError().body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(ScheduleConflictTimeException.class)
    private ResponseEntity<BaseResponse<String>> scheduleConflictHandler(ScheduleConflictTimeException ex) {
        log.error("Conflito entre a data da nova consulta e das consultas já agendadas");
        return ResponseEntity.badRequest().body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<BaseResponse<Map<String, String>>> methodNotValidHandler(MethodArgumentNotValidException ex) {
        log.error("Valores enviados não são válidos para realizar o registro");
        Map<String, String> errors = new HashMap<>();
        ex.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new BaseResponse<>(false, errors));
    }

    @ExceptionHandler(ScheduleNotFoundException.class)
    private ResponseEntity<BaseResponse<String>> scheduleNotFoundHandler(ScheduleNotFoundException ex) {
        log.error("Consulta informada não foi encontrada");
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ContractWithoutArgsException.class)
    private ResponseEntity<BaseResponse<String>> contractWithoutArgsHandler(ContractWithoutArgsException ex) {
        log.error("Não foi possível gerar o contrato, informações necessárias estão faltando");
        return ResponseEntity.badRequest().body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    private ResponseEntity<String> documentNotFoundHandler(DocumentNotFoundException ex) {
        log.error("Documento informado não foi encontrado");
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<BaseResponse<String>> dataIntegrityViolationHandler(DataIntegrityViolationException ex) {
        log.error("Dados duplicados foram enviados");
        return ResponseEntity.badRequest().body(new BaseResponse<>(false, "Dados duplicados"));
    }

    @ExceptionHandler(PlanNotFoundException.class)
    private ResponseEntity<BaseResponse<String>> planNotFoundHandler(PlanNotFoundException ex) {
        log.error("Plano informado não foi encontrado");
        return ResponseEntity.status(404).body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(PlanTemplateNotFoundException.class)
    private ResponseEntity<BaseResponse<String>> planTemplateNotFoundHandler(PlanTemplateNotFoundException ex) {
        log.error("Template de plano informado não foi encontrado");
        return ResponseEntity.status(404).body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(InvalidPlanConfigException.class)
    private ResponseEntity<BaseResponse<String>> invalidPlanConfigHandler(InvalidPlanConfigException ex) {
        log.error("Configuração inválida para criação de plano");
        return ResponseEntity.badRequest().body(new BaseResponse<>(false, ex.getMessage()));
    }
}
