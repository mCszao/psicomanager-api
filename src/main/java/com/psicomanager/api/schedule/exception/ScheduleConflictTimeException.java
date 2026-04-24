package com.psicomanager.api.schedule.exception;

import com.psicomanager.api.core.exception.CustomException;

public class ScheduleConflictTimeException extends CustomException {
    public ScheduleConflictTimeException() {
        super("Já existe uma sessão no período informado");
    }
}
