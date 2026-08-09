package com.inter.remessa.domain.model;

import com.inter.remessa.domain.exception.CnpjInvalidoException;

import java.util.regex.Pattern;

public class PessoaJuridica extends Pessoa {

    private static final Pattern CNPJ_PATTERN = Pattern.compile("^[A-Z0-9]{12}\\d{2}$");

    private String razaoSocial;
    private String cnpj;

    public PessoaJuridica(String razaoSocial, String email, String senhaHash, String cnpj) {
        super(email, senhaHash);
        if (!CNPJ_PATTERN.matcher(cnpj).matches()) throw new CnpjInvalidoException(cnpj);
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
