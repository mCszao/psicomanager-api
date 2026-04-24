package com.psicomanager.api.user.exception;

import com.psicomanager.api.core.exception.DuplicateEntryException;

public class DuplicateUserEntryException extends DuplicateEntryException {
    public DuplicateUserEntryException(String message) {
        super(message);
    }
}
