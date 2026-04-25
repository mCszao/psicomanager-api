package com.psicomanager.api.schedule.validation;

import com.psicomanager.api.core.exception.BusinessRuleException;
import com.psicomanager.api.schedule.dto.ScheduleRegisterDTO;
import com.psicomanager.api.schedule.dto.ScheduleRescheduleDTO;

import java.time.LocalDateTime;

/**
 * Valida as regras de negócio aplicadas a sessões de atendimento.
 */
public class ScheduleValidator {

    private ScheduleValidator() {}

    // region Validação de registro

    /**
     * Valida as regras de negócio do payload de criação de uma sessão.
     *
     * @param dto payload a ser validado
     * @throws BusinessRuleException se qualquer regra de negócio for violada
     */
    public static void validateRegister(ScheduleRegisterDTO dto) {
        validateDateRange(dto.dateStart(), dto.dateEnd());
        validateBatchRequirements(dto);
    }

    /**
     * Garante que {@code dateEnd}, quando informado, seja posterior a {@code dateStart}.
     */
    private static void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (end != null && !end.isAfter(start)) {
            throw new BusinessRuleException(
                    "A data de término deve ser posterior à data de início.");
        }
    }

    /**
     * Criação em lote exige {@code frequency} quando {@code sessionsCount > 1}.
     */
    private static void validateBatchRequirements(ScheduleRegisterDTO dto) {
        if (dto.sessionsCount() != null && dto.sessionsCount() > 1 && dto.frequency() == null) {
            throw new BusinessRuleException(
                    "A frequência é obrigatória para criação de múltiplas sessões.");
        }
    }

    // endregion

    // region Validação de reagendamento

    /**
     * Valida as regras de negócio do payload de reagendamento.
     *
     * @param dto payload a ser validado
     * @throws BusinessRuleException se qualquer regra de negócio for violada
     */
    public static void validateReschedule(ScheduleRescheduleDTO dto) {
        validateDateRange(dto.dateStart(), dto.dateEnd());
    }

    // endregion
}
