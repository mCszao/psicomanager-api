package com.psicomanager.api.document.exception;

import com.psicomanager.api.core.exception.CustomException;

public class ContractWithoutArgsException extends CustomException {
    public ContractWithoutArgsException() {
        super("Contrato não pode ser gerado sem os argumentos necessários");
    }

    public ContractWithoutArgsException(String message) {
        super(message);
    }
}
