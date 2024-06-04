package com.psicomanager.api.exceptions;

public abstract class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
