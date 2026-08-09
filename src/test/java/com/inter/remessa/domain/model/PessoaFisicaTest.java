package com.inter.remessa.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PessoaFisicaTest {

    @Test
    @DisplayName("Should store name, email, password and CPF and return FISICA type when created with valid data")
    void shouldStoreFieldsAndReturnFisicaTypeWhenCreatedWithValidData() {
        PessoaFisica pessoa = new PessoaFisica("João Silva", "joao@email.com", "$2a$10$hashedpassword", "123.456.789-09");

        assertThat(pessoa.getNome()).isEqualTo("João Silva");
        assertThat(pessoa.getEmail()).isEqualTo("joao@email.com");
        assertThat(pessoa.getSenhaHash()).isEqualTo("$2a$10$hashedpassword");
        assertThat(pessoa.getCpf()).isEqualTo("123.456.789-09");
        assertThat(pessoa.getTipo()).isEqualTo(TipoPessoa.FISICA);
    }
}
