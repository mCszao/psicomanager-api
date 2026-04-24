package com.psicomanager.api.auth;

import com.psicomanager.api.auth.exception.InvalidRefreshTokenException;
import com.psicomanager.api.auth.model.RefreshToken;
import com.psicomanager.api.user.model.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepo;

    @Autowired
    private TokenService tokenService;

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 não disponível", e);
        }
    }

    @Transactional
    public String createRefreshToken(User user) {
        log.info("Criando refresh token para o usuário " + user.getUsername());
        refreshTokenRepo.revokeAllByUser(user);
        String rawToken = UUID.randomUUID().toString();
        RefreshToken entity = new RefreshToken();
        entity.setTokenHash(hash(rawToken));
        entity.setUser(user);
        entity.setExpiresAt(tokenService.refreshTokenExpiration());
        entity.setRevoked(false);
        refreshTokenRepo.save(entity);
        log.info("Refresh token criado com sucesso para o usuário " + user.getUsername());
        return rawToken;
    }

    public User validateRefreshToken(String rawToken) {
        log.info("Validando refresh token");
        RefreshToken token = refreshTokenRepo
                .findByTokenHash(hash(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (token.isRevoked() || Instant.now().isAfter(token.getExpiresAt())) {
            log.warn("Refresh token inválido ou expirado");
            throw new InvalidRefreshTokenException();
        }
        return token.getUser();
    }

    @Transactional
    public void revokeAllByUser(User user) {
        log.info("Revogando refresh tokens do usuário " + user.getUsername());
        refreshTokenRepo.revokeAllByUser(user);
    }
}
