package com.psicomanager.api.exceptions;

public class DuplicateEntryException extends CustomException {
    public DuplicateEntryException(String message) {
        super(message + " já possui registro");
    }
}
