package com.psicomanager.api.domain.user.exception;

import com.psicomanager.api.core.exception.CustomException;

public class UserNotFoundException extends CustomException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
