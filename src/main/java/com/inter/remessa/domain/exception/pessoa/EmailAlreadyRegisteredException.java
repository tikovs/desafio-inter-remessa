package com.inter.remessa.domain.exception.pessoa;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(String email) {
        super("Email já cadastrado: " + email);
    }
}
