package com.inter.remessa.domain.model;

import com.inter.remessa.domain.exception.InvalidCpfException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.regex.Pattern;

@Entity
@DiscriminatorValue("FISICA")
public class PessoaFisica extends Pessoa {

    private static final Pattern CPF_PATTERN = Pattern.compile("^\\d{11}$");

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String cpf;

    protected PessoaFisica() {}

    public PessoaFisica(String name, String email, String passwordHash, String cpf) {
        super(email, passwordHash);
        if (!CPF_PATTERN.matcher(cpf).matches()) throw new InvalidCpfException(cpf);
        this.name = name;
        this.cpf = cpf;
    }

    @Override
    public TipoPessoa getType() {
        return TipoPessoa.FISICA;
    }

    public String getName() { return name; }
    public String getCpf() { return cpf; }
}
