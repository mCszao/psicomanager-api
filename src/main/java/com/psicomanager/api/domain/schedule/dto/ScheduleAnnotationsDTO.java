package com.psicomanager.api.domain.schedule.dto;

/**
 * Payload para atualização de anotações de uma sessão.
 *
 * @param annotations Texto livre com as anotações do psicólogo.
 *                    Aceita null para limpar o campo.
 */
public record ScheduleAnnotationsDTO(String annotations) {}
