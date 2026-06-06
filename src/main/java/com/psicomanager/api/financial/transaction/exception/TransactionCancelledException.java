package com.psicomanager.api.financial.transaction.exception;

import com.psicomanager.api.core.exception.CustomException;

public class TransactionCancelledException extends CustomException {
    public TransactionCancelledException() {
        super("Esta transação está cancelada e não pode ser alterada");
    }
}
