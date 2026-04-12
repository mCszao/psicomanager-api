package com.psicomanager.api.domain.patient.exception;

import com.psicomanager.api.core.exception.CustomException;

public class PatientNotFoundException extends CustomException {
    public PatientNotFoundException() {
        super("Paciente não encontrado");
    }
}
