package com.psicomanager.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRegisterDTO(
        @NotBlank
        String username,
        @NotBlank
        String password,
        @Pattern(regexp = "\\d{11,15}")
        String phone,
        @Email
        String email
) {}
