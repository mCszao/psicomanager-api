package com.psicomanager.api.infra.config;

import com.psicomanager.api.dtos.BaseResponse;
import com.psicomanager.api.exceptions.DuplicateEntryException;
import com.psicomanager.api.exceptions.patient.PatientNotFoundException;
import com.psicomanager.api.exceptions.schedule.ScheduleConflictTimeException;
import com.psicomanager.api.exceptions.user.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    private ResponseEntity<BaseResponse> userNotFoundHandler(UserNotFoundException ex){
        return ResponseEntity.badRequest().body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEntryException.class)
    private ResponseEntity<BaseResponse> duplicateUserEntryHandler(DuplicateEntryException ex){
        return ResponseEntity.badRequest().body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(PatientNotFoundException.class)
    private ResponseEntity<BaseResponse> patientNotFoundHandler(PatientNotFoundException ex){
        return ResponseEntity.badRequest().body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(ScheduleConflictTimeException.class)
    private ResponseEntity<BaseResponse> scheduleConflictHandler(ScheduleConflictTimeException ex){
        return ResponseEntity.badRequest().body(new BaseResponse<>(false, ex.getMessage()));
    }
}
