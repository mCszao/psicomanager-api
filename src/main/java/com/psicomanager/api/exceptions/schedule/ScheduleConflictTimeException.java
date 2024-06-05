package com.psicomanager.api.exceptions.schedule;

import com.psicomanager.api.exceptions.CustomException;

public class ScheduleConflictTimeException extends CustomException {
    public ScheduleConflictTimeException(String message) {
        super(message);
    }
}
