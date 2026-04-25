package com.psicomanager.api.plan.exception;

import com.psicomanager.api.core.exception.CustomException;

public class PlanTemplateNotFoundException extends CustomException {
    public PlanTemplateNotFoundException() {
        super("Template de plano não encontrado");
    }
}
