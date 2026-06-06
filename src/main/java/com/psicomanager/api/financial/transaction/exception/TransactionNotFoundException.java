package com.psicomanager.api.financial.transaction.exception;

import com.psicomanager.api.core.exception.CustomException;

public class TransactionNotFoundException extends CustomException {
    public TransactionNotFoundException() {
        super("Transação não encontrada");
    }
}
