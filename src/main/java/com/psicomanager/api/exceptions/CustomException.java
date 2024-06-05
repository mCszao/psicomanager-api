package com.psicomanager.api.exceptions;

public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
