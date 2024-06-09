package com.psicomanager.api.core.exception;

public class DuplicateEntryException extends CustomException {
    public DuplicateEntryException(String message) {
        super(message + " já possui registro");
    }
}
