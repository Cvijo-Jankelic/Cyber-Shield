package com.project.cybershield.exceptions;

public class EmailAlreadyExists extends RegistrationException {
    public EmailAlreadyExists(String message) {
        super("Email already exists");
    }
}
