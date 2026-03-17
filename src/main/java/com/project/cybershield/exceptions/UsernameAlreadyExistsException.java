package com.project.cybershield.exceptions;

public class UsernameAlreadyExistsException extends RegistrationException {
    public UsernameAlreadyExistsException(String message) {
        super("Username is not available");
    }

    public UsernameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
