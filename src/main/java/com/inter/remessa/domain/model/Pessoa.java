package com.inter.remessa.domain.model;

import com.inter.remessa.domain.exception.InvalidEmailException;

import java.util.regex.Pattern;

public abstract class Pessoa {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private String email;
    private String passwordHash;

    protected Pessoa(String email, String passwordHash) {
        if (!EMAIL_PATTERN.matcher(email).matches()) throw new InvalidEmailException(email);
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
    }

    public abstract TipoPessoa getType();

    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
}
