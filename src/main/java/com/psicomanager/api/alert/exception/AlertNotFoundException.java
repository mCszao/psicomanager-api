package com.psicomanager.api.alert.exception;

import com.psicomanager.api.core.exception.CustomException;

public class AlertNotFoundException extends CustomException {
    public AlertNotFoundException() {
        super("Aviso não encontrado");
    }
}
