package com.psicomanager.api.schedule.exception;

import com.psicomanager.api.core.exception.CustomException;

public class ScheduleAlreadyAbsentException extends CustomException {
    public ScheduleAlreadyAbsentException() {
        super("Apenas sessões abertas podem ser marcadas como falta");
    }
}
