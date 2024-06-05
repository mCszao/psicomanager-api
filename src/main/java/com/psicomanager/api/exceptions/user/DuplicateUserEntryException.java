package com.psicomanager.api.exceptions.user;

import com.psicomanager.api.exceptions.CustomException;
import com.psicomanager.api.exceptions.DuplicateEntryException;

public class DuplicateUserEntryException extends DuplicateEntryException {

    public DuplicateUserEntryException(String message) {
        super(message);
    }
}
