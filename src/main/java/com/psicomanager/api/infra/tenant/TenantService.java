package com.psicomanager.api.infra.tenant;

import com.psicomanager.api.core.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

/**
 * Helper injetável que expõe o tenant atual com validação.
 *
 * <p>Services injetam este componente e chamam {@link #required()} para obter
 * o {@code organization_id} garantindo que o contexto está definido.</p>
 */
@Component
public class TenantService {

    /**
     * Retorna o ID da organização ativa, lançando exceção se não houver tenant definido.
     *
     * @return organizationId da requisição atual
     * @throws BusinessRuleException se o usuário não tiver organização ativa
     */
    public String required() {
        String orgId = TenantContext.get();
        if (orgId == null) {
            throw new BusinessRuleException(
                    "Nenhuma organização ativa. Crie ou ingresse em uma organização antes de continuar.");
        }
        return orgId;
    }

    /**
     * Retorna o ID da organização ativa, ou {@code null} se não houver.
     * Usar apenas em contextos onde o tenant é opcional.
     *
     * @return organizationId ou null
     */
    public String optional() {
        return TenantContext.get();
    }
}
