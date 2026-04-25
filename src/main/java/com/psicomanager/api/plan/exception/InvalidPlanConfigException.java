package com.psicomanager.api.plan.exception;

import com.psicomanager.api.core.exception.CustomException;

public class InvalidPlanConfigException extends CustomException {
    public InvalidPlanConfigException(String message) {
        super(message);
    }
}
