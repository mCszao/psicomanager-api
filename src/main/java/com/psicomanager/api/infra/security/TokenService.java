package com.psicomanager.api.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.psicomanager.api.repositories.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@Slf4j
public class TokenService {
    @Value("${security.jwt.token.secret}")
    private String secretKey;


    public String generateJWT(User user){
        log.info("Gerando token jwt para o usuário "+user.getUsername());
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            return JWT.create()
                    .withIssuer("psicomanager-auth-login")
                    .withSubject(user.getUsername())
                    .withExpiresAt(this.generateExpireDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new RuntimeException("JWT error generate", exception);
        }
    }

    public String validateJWT(String token){
        log.info("Validando token recebido");
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        try {
            return JWT.require(algorithm)
                    .withIssuer("psicomanager-auth-login")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch(JWTVerificationException exception){
            return null;
        }
    }

    public Instant generateExpireDate(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-04:00"));
    }
}
