package com.inter.remessa.domain.model;

import com.inter.remessa.domain.exception.EmailInvalidoException;

import java.util.regex.Pattern;

public abstract class Pessoa {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private String email;
    private String senhaHash;

    protected Pessoa(String email, String senhaHash) {
        if (!EMAIL_PATTERN.matcher(email).matches()) throw new EmailInvalidoException(email);
        this.email = email.toLowerCase();
        this.senhaHash = senhaHash;
    }

    public abstract TipoPessoa getTipo();

    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
}
