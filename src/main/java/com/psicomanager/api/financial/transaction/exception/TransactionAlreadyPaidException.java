package com.psicomanager.api.financial.transaction.exception;

import com.psicomanager.api.core.exception.CustomException;

public class TransactionAlreadyPaidException extends CustomException {
    public TransactionAlreadyPaidException() {
        super("Esta transação já foi liquidada");
    }
}
