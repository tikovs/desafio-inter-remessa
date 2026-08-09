package com.inter.remessa.domain.model;

public class PessoaJuridica extends Pessoa {

    private String razaoSocial;
    private String cnpj;

    public PessoaJuridica(String razaoSocial, String email, String senhaHash, String cnpj) {
        super(email, senhaHash);
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
    }

    @Override
    public TipoPessoa getTipo() {
        return TipoPessoa.JURIDICA;
    }

    public String getRazaoSocial() { return razaoSocial; }
    public String getCnpj() { return cnpj; }
}
