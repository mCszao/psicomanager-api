package com.psicomanager.api.alert.dto;

import com.psicomanager.api.alert.enums.AlertScope;
import com.psicomanager.api.alert.enums.AlertType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Representação de um aviso retornado ao frontend.
 */
public record AlertResponseDTO(
        String id,
        String patientId,
        String sessionId,
        AlertType type,
        AlertScope scope,
        String message,
        Boolean isActive,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime createdAt
) {}
