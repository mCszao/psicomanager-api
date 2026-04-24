package com.psicomanager.api.auth;

import com.psicomanager.api.auth.model.RefreshToken;
import com.psicomanager.api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE refresh_tokens rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllByUser(User user);
}
