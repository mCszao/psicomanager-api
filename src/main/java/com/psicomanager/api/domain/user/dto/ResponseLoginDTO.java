package com.psicomanager.api.domain.user.dto;

/**
 * Response body returned after a successful login.
 * Tokens (access and refresh) are delivered via HttpOnly cookies — not in the body.
 */
public record ResponseLoginDTO(String username) {}
