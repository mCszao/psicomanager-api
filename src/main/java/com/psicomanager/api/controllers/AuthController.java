package com.psicomanager.api.controllers;

import com.psicomanager.api.domain.user.dto.ResponseLoginDTO;
import com.psicomanager.api.domain.user.model.User;
import com.psicomanager.api.core.dto.BaseResponse;
import com.psicomanager.api.domain.user.dto.UserLoginDTO;
import com.psicomanager.api.domain.user.dto.UserRegisterDTO;
import com.psicomanager.api.services.MyUserDetailsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private MyUserDetailsService authService;

    @Autowired
    private AuthenticationManager authManager;


    @PostMapping("/signIn")
    public ResponseEntity<BaseResponse<ResponseLoginDTO>> signIn(@RequestBody @Valid UserLoginDTO body){
        log.info("POST: /auth/signIn");
        log.info("Tentativa de login do usuário: "+body.username());
        var authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(body.username(), body.password()));
        var userLogin = authService.login(body, (User) authentication.getPrincipal());
        return ResponseEntity.ok(new BaseResponse<>(true, userLogin));
    }

    @PostMapping("/signUp")
    public ResponseEntity<BaseResponse<String>> signUp(@RequestBody @Valid UserRegisterDTO body){
        log.info("POST: /auth/signUp");
        authService.register(body);
        return ResponseEntity.ok(new BaseResponse<>(true, "Cadastro realizado com sucesso!"));
    }
}
