package com.psicomanager.api.organization.exception;
import com.psicomanager.api.core.exception.CustomException;
public class AlreadyMemberException extends CustomException {
    public AlreadyMemberException() { super("Você já faz parte desta organização"); }
}
