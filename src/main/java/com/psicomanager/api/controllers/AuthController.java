package com.psicomanager.api.controllers;

import com.psicomanager.api.domain.user.User;
import com.psicomanager.api.dtos.BaseResponse;
import com.psicomanager.api.domain.user.UserLoginDTO;
import com.psicomanager.api.domain.user.UserRegisterDTO;
import com.psicomanager.api.infra.security.TokenService;
import com.psicomanager.api.services.MyUserDetailsService;
import jakarta.validation.Valid;
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
public class AuthController {

    @Autowired
    private TokenService tokenService;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private MyUserDetailsService authService;
    @PostMapping("/login")
    public ResponseEntity<Object> signIn(@RequestBody @Valid UserLoginDTO body){
        var authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(body.username(), body.password()));
        var token = tokenService.generateJWT((User) authentication.getPrincipal());
        return ResponseEntity.ok(new UserLoginDTO(body.username(),token));
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse> signUp(@RequestBody @Valid UserRegisterDTO body){
        if(authService.save(body)) return ResponseEntity.ok(new BaseResponse(true, "Cadastro realizado com sucesso!"));
        return ResponseEntity.badRequest().body(new BaseResponse(false, "Registro não realizado!"));
    }
}
