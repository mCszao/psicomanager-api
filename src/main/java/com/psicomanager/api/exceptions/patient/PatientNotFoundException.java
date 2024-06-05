package com.psicomanager.api.exceptions.patient;

import com.psicomanager.api.exceptions.CustomException;

public class PatientNotFoundException extends CustomException {
    public PatientNotFoundException(String message) {
        super(message);
    }
}
