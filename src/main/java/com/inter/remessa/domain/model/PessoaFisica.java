package com.inter.remessa.domain.model;

import com.inter.remessa.domain.exception.CpfInvalidoException;

import java.util.regex.Pattern;

public class PessoaFisica extends Pessoa {

    private static final Pattern CPF_PATTERN = Pattern.compile("^\\d{11}$");

    private String nome;
    private String cpf;

    public PessoaFisica(String nome, String email, String senhaHash, String cpf) {
        super(email, senhaHash);
        if (!CPF_PATTERN.matcher(cpf).matches()) throw new CpfInvalidoException(cpf);
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
