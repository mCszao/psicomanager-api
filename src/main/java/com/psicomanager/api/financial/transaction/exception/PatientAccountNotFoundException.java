package com.psicomanager.api.financial.transaction.exception;

import com.psicomanager.api.core.exception.CustomException;

public class PatientAccountNotFoundException extends CustomException {
    public PatientAccountNotFoundException() {
        super("Conta financeira do paciente não encontrada");
    }
}
