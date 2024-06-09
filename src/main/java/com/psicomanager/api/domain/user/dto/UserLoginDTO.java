package com.psicomanager.api.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginDTO(
        @NotBlank
        String username,
        @NotBlank
        String password) {}