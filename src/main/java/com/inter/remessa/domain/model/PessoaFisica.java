package com.inter.remessa.domain.model;

public class PessoaFisica extends Pessoa {

    private String nome;
    private String cpf;

    public PessoaFisica(String nome, String email, String senhaHash, String cpf) {
        super(email, senhaHash);
        this.nome = nome;
        this.cpf = cpf;
    }

    @Override
    public TipoPessoa getTipo() {
        return TipoPessoa.FISICA;
    }

    public String getNome() { return nome; }

    public String getCpf() { return cpf; }
}
