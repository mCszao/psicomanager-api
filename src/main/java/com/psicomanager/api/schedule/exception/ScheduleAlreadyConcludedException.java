package com.psicomanager.api.schedule.exception;

import com.psicomanager.api.core.exception.CustomException;

public class ScheduleAlreadyConcludedException extends CustomException {
    public ScheduleAlreadyConcludedException() {
        super("Apenas sessões abertas podem ser concluídas");
    }
}
