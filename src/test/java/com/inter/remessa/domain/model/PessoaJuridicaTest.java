package com.inter.remessa.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PessoaJuridicaTest {

    @Test
    @DisplayName("Should store name, email, password hash and CNPJ and return JURIDICA type when created with valid data")
    void shouldStoreFieldsAndReturnJuridicaTypeWhenCreatedWithValidData() {
        PessoaJuridica pessoa = new PessoaJuridica("Empresa XPTO", "contato@xpto.com", "$2a$10$hashedpassword", "12.345.678/0001-90");

        assertThat(pessoa.getRazaoSocial()).isEqualTo("Empresa XPTO");
        assertThat(pessoa.getEmail()).isEqualTo("contato@xpto.com");
        assertThat(pessoa.getSenhaHash()).isEqualTo("$2a$10$hashedpassword");
        assertThat(pessoa.getCnpj()).isEqualTo("12.345.678/0001-90");
        assertThat(pessoa.getTipo()).isEqualTo(TipoPessoa.JURIDICA);
    }
}
