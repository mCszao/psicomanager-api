package com.psicomanager.api.organization.exception;
import com.psicomanager.api.core.exception.CustomException;
public class NotMemberException extends CustomException {
    public NotMemberException() { super("Você não faz parte desta organização"); }
}
