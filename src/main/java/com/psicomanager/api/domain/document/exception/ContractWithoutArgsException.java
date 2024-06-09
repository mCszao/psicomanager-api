package com.psicomanager.api.domain.document.exception;

import com.psicomanager.api.core.exception.CustomException;

public class ContractWithoutArgsException extends CustomException {
    public ContractWithoutArgsException(String message) {
        super(message);
    }
}
