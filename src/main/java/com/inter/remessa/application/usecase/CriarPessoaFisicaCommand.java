package com.inter.remessa.application.usecase;

public record CriarPessoaFisicaCommand(String name, String email, String password, String cpf) {}
