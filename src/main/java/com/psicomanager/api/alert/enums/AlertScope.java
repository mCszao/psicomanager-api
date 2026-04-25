package com.psicomanager.api.alert.enums;

public enum AlertScope {
    /** Aviso vinculado ao paciente — persiste até descarte manual. */
    PATIENT,
    /** Aviso vinculado a uma sessão específica — desativado automaticamente ao concluir a sessão. */
    SESSION
}
