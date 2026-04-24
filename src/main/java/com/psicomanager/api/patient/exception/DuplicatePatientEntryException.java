package com.psicomanager.api.patient.exception;

import com.psicomanager.api.core.exception.DuplicateEntryException;

public class DuplicatePatientEntryException extends DuplicateEntryException {
    public DuplicatePatientEntryException(String message) {
        super(message);
    }
}
