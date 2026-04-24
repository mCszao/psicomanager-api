package com.psicomanager.api.schedule.exception;

import com.psicomanager.api.core.exception.CustomException;

public class ScheduleAlreadyCancelledException extends CustomException {
    public ScheduleAlreadyCancelledException() {
        super("Apenas sessões abertas podem ser canceladas");
    }
}
