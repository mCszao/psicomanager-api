package com.psicomanager.api.services;

import com.psicomanager.api.domain.auth.exception.InvalidRefreshTokenException;
import com.psicomanager.api.domain.auth.model.RefreshToken;
import com.psicomanager.api.repositories.auth.RefreshTokenRepository;
import com.psicomanager.api.repositories.user.model.User;
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

    // region Token hashing

    /**
     * Produces a Base64-encoded SHA-256 hash of the raw token.
     * Only the hash is persisted — the raw value never touches the database.
     */
    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 não disponível", e);
        }
    }

    // endregion

    // region Public API

    /**
     * Creates and persists a new refresh token for the given user.
     * Any previously existing tokens for that user are revoked first (rotation strategy).
     *
     * @param user the owner of the token
     * @return the raw (unhashed) token value to be sent to the client
     */
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

    /**
     * Validates the raw refresh token received from the client.
     * Throws {@link InvalidRefreshTokenException} if the token is not found,
     * revoked, or expired.
     *
     * @param rawToken the token value read from the HttpOnly cookie
     * @return the {@link User} associated with the valid token
     */
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

    /**
     * Revokes all refresh tokens belonging to the given user (logout).
     *
     * @param user the user whose tokens should be revoked
     */
    @Transactional
    public void revokeAllByUser(User user) {
        log.info("Revogando refresh tokens do usuário " + user.getUsername());
        refreshTokenRepo.revokeAllByUser(user);
    }

    // endregion
}
