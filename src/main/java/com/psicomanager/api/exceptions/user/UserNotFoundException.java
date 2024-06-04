package com.psicomanager.api.exceptions.user;

import com.psicomanager.api.exceptions.CustomException;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
