package com.psicomanager.api.alert.dto;

import com.psicomanager.api.alert.enums.AlertScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload de criação de um aviso manual.
 */
public record AlertRegisterDTO(

        @NotBlank
        String patientId,

        /**
         * ID da sessão. Obrigatório quando {@code scope} é {@code SESSION}.
         */
        String sessionId,

        @NotNull
        AlertScope scope,

        @NotBlank
        String message

) {}
