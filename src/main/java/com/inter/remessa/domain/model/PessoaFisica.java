package com.inter.remessa.domain.model;

public class PessoaFisica extends Pessoa {

    private String cpf;

    public PessoaFisica(String nome, String email, String senhaHash, String cpf) {
        super(nome, email, senhaHash);
        this.cpf = cpf;
    }

    @Override
    public TipoPessoa getTipo() {
        return TipoPessoa.FISICA;
    }

    public String getCpf() { return cpf; }
}
