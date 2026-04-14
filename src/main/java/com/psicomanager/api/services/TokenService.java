package com.psicomanager.api.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.psicomanager.api.repositories.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
public class TokenService {

    @Value("${security.jwt.token.secret}")
    private String secretKey;

    /**
     * Result of a JWT validation attempt.
     *
     * @param subject   the username extracted from the token, or {@code null} if invalid/expired
     * @param isExpired {@code true} when the token signature is valid but the expiry has passed
     */
    public record ValidationResult(String subject, boolean isExpired) {
        public static ValidationResult valid(String subject) {
            return new ValidationResult(subject, false);
        }

        public static ValidationResult expired() {
            return new ValidationResult(null, true);
        }

        public static ValidationResult invalid() {
            return new ValidationResult(null, false);
        }
    }

    /**
     * Generates a short-lived JWT access token for the given user.
     * Expires in 15 minutes from now (UTC).
     */
    public String generateAccessToken(User user) {
        log.info("Gerando access token para o usuário " + user.getUsername());
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            return JWT.create()
                    .withIssuer("psicomanager-auth-login")
                    .withSubject(user.getUsername())
                    .withExpiresAt(accessTokenExpiration())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar access token", exception);
        }
    }

    /**
     * Validates a JWT and returns a {@link ValidationResult} distinguishing
     * between a valid token, an expired token, and an otherwise invalid token.
     */
    public ValidationResult validateJWT(String token) {
        log.info("Validando token recebido");
        if (token == null) return ValidationResult.invalid();

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        try {
            String subject = JWT.require(algorithm)
                    .withIssuer("psicomanager-auth-login")
                    .build()
                    .verify(token)
                    .getSubject();
            return ValidationResult.valid(subject);
        } catch (TokenExpiredException e) {
            log.warn("Token expirado recebido");
            return ValidationResult.expired();
        } catch (JWTVerificationException e) {
            return ValidationResult.invalid();
        }
    }

    /**
     * Access token expiration: 15 minutes from now (UTC).
     */
    public Instant accessTokenExpiration() {
        return Instant.now().plus(15, ChronoUnit.MINUTES);
    }

    /**
     * Refresh token expiration: 7 days from now (UTC).
     */
    public Instant refreshTokenExpiration() {
        return Instant.now().plus(7, ChronoUnit.DAYS);
    }
}
