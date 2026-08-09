package com.inter.remessa.domain.model;

public abstract class Pessoa {


    private String email;
    private String senhaHash;

    protected Pessoa(String email, String senhaHash) {
        this.email = email;
        this.senhaHash = senhaHash;
    }

    public abstract TipoPessoa getTipo();


    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
}
