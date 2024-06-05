package com.psicomanager.api.exceptions.patient;

import com.psicomanager.api.exceptions.CustomException;

public class PatientNotFound extends CustomException {
    public PatientNotFound(String message) {
        super(message);
    }
}
