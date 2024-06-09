package com.psicomanager.api.domain.schedule.exception;

import com.psicomanager.api.core.exception.CustomException;

public class ScheduleConflictTimeException extends CustomException {
    public ScheduleConflictTimeException(String message) {
        super(message);
    }
}
