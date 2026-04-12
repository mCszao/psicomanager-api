package com.psicomanager.api.domain.schedule.exception;

import com.psicomanager.api.core.exception.CustomException;

public class ScheduleNotFoundException extends CustomException {
    public ScheduleNotFoundException() {
        super("Agendamento não encontrado");
    }
}
