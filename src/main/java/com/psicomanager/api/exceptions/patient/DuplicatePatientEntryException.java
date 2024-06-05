package com.psicomanager.api.exceptions.patient;

import com.psicomanager.api.exceptions.DuplicateEntryException;

public class DuplicatePatientEntryException extends DuplicateEntryException {
    public DuplicatePatientEntryException(String message) {
        super(message);
    }
}
