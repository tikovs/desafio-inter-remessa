package com.inter.remessa.application.usecase;

public record CriarPessoaJuridicaCommand(String razaoSocial, String email, String password, String cnpj) {}
