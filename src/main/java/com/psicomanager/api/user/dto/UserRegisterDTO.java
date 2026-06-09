package com.psicomanager.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterDTO(
        @NotBlank
        @Size(min = 3, max = 255)
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "Username deve conter apenas letras, números, ponto, hífen ou underscore — sem espaços"
        )
        String username,
        @NotBlank
        @Size(max = 255)
        String name,
        @NotBlank
        String password,
        @Pattern(regexp = "\\d{10,11}")
        String phone,
        @Email
        String email
) {}
