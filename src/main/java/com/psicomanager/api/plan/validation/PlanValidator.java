package com.psicomanager.api.plan.validation;

import com.psicomanager.api.core.exception.BusinessRuleException;
import com.psicomanager.api.plan.dto.PlanRegisterDTO;
import com.psicomanager.api.plan.model.Plan;

/**
 * Valida as regras de negócio aplicadas a planos de atendimento.
 * <p>
 * As validações aqui presentes vão além das constraints Jakarta (campos nulos, tamanho, etc.)
 * e garantem a integridade lógica dos dados — como a exigência de {@code sessionsCount}
 * em planos finitos ou de {@code frequency} em qualquer tipo de plano.
 * </p>
 */
public class PlanValidator {

    private PlanValidator() {
    }

    // region Validação de registro

    /**
     * Valida as regras de negócio do payload de criação de um plano.
     *
     * @param dto payload a ser validado
     * @throws BusinessRuleException se qualquer regra de negócio for violada
     */
    public static void validateRegister(PlanRegisterDTO dto) {
        validateFrequency(dto);
        validateFinitePlan(dto);
        validateSessionGeneration(dto);
    }

    /**
     * Garante que {@code frequency} seja informada para qualquer tipo de plano.
     * Planos sem frequência não permitem geração automática nem o botão "Lançar mais sessões".
     */
    private static void validateFrequency(PlanRegisterDTO dto) {
        if (dto.frequency() == null) {
            throw new BusinessRuleException(
                    "A frequência é obrigatória para qualquer tipo de plano.");
        }
    }

    /**
     * Planos finitos exigem {@code sessionsCount} definido.
     * Sem ele não é possível calcular o encerramento automático nem o progresso.
     */
    private static void validateFinitePlan(PlanRegisterDTO dto) {
        if (!dto.isContinuous()) {
            if (dto.sessionsCount() == null || dto.sessionsCount() < 1) {
                throw new BusinessRuleException(
                        "Planos finitos exigem o número de sessões informado.");
            }
        }
    }

    /**
     * Quando {@code generateSessions} é {@code true}, o horário de início e o tipo
     * de atendimento são obrigatórios para que as sessões possam ser criadas corretamente.
     */
    private static void validateSessionGeneration(PlanRegisterDTO dto) {
        if (dto.generateSessions()) {
            if (dto.sessionStartTime() == null || dto.sessionStartTime().isBlank()) {
                throw new BusinessRuleException(
                        "Para gerar sessões automaticamente é necessário informar o horário de início.");
            }
            if (dto.attendanceType() == null) {
                throw new BusinessRuleException(
                        "Para gerar sessões automaticamente é necessário informar o tipo de atendimento (presencial ou remoto).");
            }
        }
    }

    // endregion

    // region Validação de estado

    /**
     * Garante que o plano esteja ativo antes de vincular novas sessões a ele.
     *
     * @param plan plano a ser verificado
     * @throws BusinessRuleException se o plano estiver inativo
     */
    public static void validatePlanIsActive(Plan plan) {
        if (!Boolean.TRUE.equals(plan.getIsActive())) {
            throw new BusinessRuleException(
                    "Não é possível vincular sessões a um plano encerrado.");
        }
    }

    // endregion
}
