package com.inter.remessa.domain.model;

import com.inter.remessa.domain.exception.pessoa.InvalidCnpjException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.regex.Pattern;

@Entity
@DiscriminatorValue("JURIDICA")
public class PessoaJuridica extends Pessoa {

    private static final Pattern CNPJ_PATTERN = Pattern.compile("^[A-Z0-9]{12}\\d{2}$");

    @Column(nullable = false)
    private String razaoSocial;

    @Column(unique = true, nullable = false)
    private String cnpj;

    protected PessoaJuridica() {}

    public PessoaJuridica(String razaoSocial, String email, String passwordHash, String cnpj) {
        super(email, passwordHash);
        if (!CNPJ_PATTERN.matcher(cnpj).matches()) throw new InvalidCnpjException(cnpj);
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
    }

    @Override
    public TipoPessoa getType() {
        return TipoPessoa.JURIDICA;
    }

    public String getRazaoSocial() { return razaoSocial; }
    public String getCnpj() { return cnpj; }
}
