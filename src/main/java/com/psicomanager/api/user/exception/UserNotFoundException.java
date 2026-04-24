package com.psicomanager.api.user.exception;

import com.psicomanager.api.core.exception.CustomException;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException() {
        super("Usuário não encontrado");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
