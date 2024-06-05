package com.psicomanager.api.exceptions.user;

import com.psicomanager.api.exceptions.CustomException;

public class DuplicateUserEntryException extends CustomException {
    public DuplicateUserEntryException(String message) {
        super(message);
    }
}
