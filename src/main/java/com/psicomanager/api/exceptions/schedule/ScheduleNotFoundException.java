package com.psicomanager.api.exceptions.schedule;

import com.psicomanager.api.exceptions.CustomException;

public class ScheduleNotFoundException extends CustomException {
    public ScheduleNotFoundException(String message) {
        super(message);
    }
}
