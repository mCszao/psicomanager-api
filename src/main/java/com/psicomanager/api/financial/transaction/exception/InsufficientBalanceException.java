package com.psicomanager.api.financial.transaction.exception;

import com.psicomanager.api.core.exception.CustomException;

public class InsufficientBalanceException extends CustomException {
    public InsufficientBalanceException() {
        super("Saldo insuficiente para esta operação");
    }
}
