package com.psicomanager.api.domain.user.exception;

import com.psicomanager.api.core.exception.CustomException;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException() {
        super("Usuário não encontrado");
    }
}
