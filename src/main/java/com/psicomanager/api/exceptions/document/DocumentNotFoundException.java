package com.psicomanager.api.exceptions.document;

import com.psicomanager.api.exceptions.CustomException;

public class DocumentNotFoundException extends CustomException {
    public DocumentNotFoundException(String message) {
        super(message);
    }
}
