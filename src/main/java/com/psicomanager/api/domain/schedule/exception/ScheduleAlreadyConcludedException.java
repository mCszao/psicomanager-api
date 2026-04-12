package com.psicomanager.api.domain.schedule.exception;

import com.psicomanager.api.core.exception.CustomException;

public class ScheduleAlreadyConcludedException extends CustomException {
    public ScheduleAlreadyConcludedException(String message) {
        super(message);
    }
}
