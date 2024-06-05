package com.psicomanager.api.infra.config;

import com.psicomanager.api.dtos.BaseResponse;
import com.psicomanager.api.exceptions.user.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
@ControllerAdvice
public class ExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    private ResponseEntity<BaseResponse> userNotFoundHandler(){

    }
}
