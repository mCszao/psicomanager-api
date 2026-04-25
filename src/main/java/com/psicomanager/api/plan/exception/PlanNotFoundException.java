package com.psicomanager.api.plan.exception;

import com.psicomanager.api.core.exception.CustomException;

public class PlanNotFoundException extends CustomException {
    public PlanNotFoundException() {
        super("Plano não encontrado");
    }
}
