package com.psicomanager.api.schedule.exception;

import com.psicomanager.api.core.exception.CustomException;

public class ScheduleAlreadyRescheduledException extends CustomException {
    public ScheduleAlreadyRescheduledException() {
        super("Apenas sessões abertas podem ser reagendadas");
    }
}
