package com.psicomanager.api.core.exception;

/**
 * Exceção lançada quando uma regra de negócio é violada.
 * <p>
 * Diferente de {@link CustomException} (usada para erros de entidade não encontrada),
 * esta exceção representa inconsistências lógicas nos dados enviados — como um plano
 * finito sem número de sessões, ou geração de sessões sem horário informado.
 * </p>
 */
public class BusinessRuleException extends CustomException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
