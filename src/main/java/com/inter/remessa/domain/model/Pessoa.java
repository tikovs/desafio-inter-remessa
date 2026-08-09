package com.inter.remessa.domain.model;

public abstract class Pessoa {

    private String nome;
    private String email;
    private String senhaHash;

    protected Pessoa(String nome, String email, String senhaHash) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    public abstract TipoPessoa getTipo();

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
}
