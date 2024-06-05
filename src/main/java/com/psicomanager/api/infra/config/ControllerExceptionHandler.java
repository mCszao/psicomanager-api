package com.psicomanager.api.infra.config;

import com.psicomanager.api.dtos.BaseResponse;
import com.psicomanager.api.exceptions.user.DuplicateUserEntryException;
import com.psicomanager.api.exceptions.user.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    private ResponseEntity<BaseResponse> userNotFoundHandler(UserNotFoundException ex){
        return ResponseEntity.badRequest().body(new BaseResponse(false, ex.getMessage()));
    }

    @ExceptionHandler(DuplicateUserEntryException.class)
    private ResponseEntity<BaseResponse> duplicateUserEntryHandler(DuplicateUserEntryException ex){
        return ResponseEntity.badRequest().body(new BaseResponse(false, ex.getMessage()));
    }
}
