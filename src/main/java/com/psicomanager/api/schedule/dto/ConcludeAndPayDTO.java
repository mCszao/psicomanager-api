package com.psicomanager.api.schedule.dto;

import com.psicomanager.api.financial.transaction.enums.PaymentMethodEnum;
import jakarta.validation.constraints.NotNull;

/**
 * Payload de "concluir e pagar" — conclui a sessão e liquida a cobrança gerada
 * com a forma de pagamento informada.
 */
public record ConcludeAndPayDTO(
        @NotNull PaymentMethodEnum paymentMethod
) {}
