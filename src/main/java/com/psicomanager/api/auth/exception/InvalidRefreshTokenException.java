package com.psicomanager.api.auth.exception;

import com.psicomanager.api.core.exception.CustomException;

public class InvalidRefreshTokenException extends CustomException {
    public InvalidRefreshTokenException() {
        super("Sessão expirada. Faça login novamente.");
    }
}
