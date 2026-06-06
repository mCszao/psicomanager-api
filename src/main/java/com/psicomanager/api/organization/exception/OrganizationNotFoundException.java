package com.psicomanager.api.organization.exception;
import com.psicomanager.api.core.exception.CustomException;
public class OrganizationNotFoundException extends CustomException {
    public OrganizationNotFoundException() { super("Organização não encontrada"); }
}
