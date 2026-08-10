package com.inter.remessa.application.usecase.pessoa;

public record CriarPessoaJuridicaCommand(String razaoSocial, String email, String password, String cnpj) {}
