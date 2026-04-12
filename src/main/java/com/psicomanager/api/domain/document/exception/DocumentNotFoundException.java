package com.psicomanager.api.domain.document.exception;

import com.psicomanager.api.core.exception.CustomException;

public class DocumentNotFoundException extends CustomException {
    public DocumentNotFoundException() {
        super("Documento não encontrado");
    }
}
