package com.inter.remessa.domain.model;

import com.inter.remessa.domain.exception.pessoa.InvalidCnpjException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PessoaJuridicaTest {

    @Test
    @DisplayName("Should store name, email, password hash and CNPJ and return JURIDICA type when created with valid data")
    void shouldStoreFieldsAndReturnJuridicaTypeWhenCreatedWithValidData() {
        PessoaJuridica pessoa = new PessoaJuridica("Empresa XPTO", "contato@xpto.com", "$2a$10$hashedpassword", "12345678000190");

        assertThat(pessoa.getRazaoSocial()).isEqualTo("Empresa XPTO");
        assertThat(pessoa.getEmail()).isEqualTo("contato@xpto.com");
        assertThat(pessoa.getPasswordHash()).isEqualTo("$2a$10$hashedpassword");
        assertThat(pessoa.getCnpj()).isEqualTo("12345678000190");
        assertThat(pessoa.getType()).isEqualTo(TipoPessoa.JURIDICA);
    }

    @Test
    @DisplayName("Should accept CNPJ with new alphanumeric format (12 uppercase alphanumeric + 2 digit check digits)")
    void shouldAcceptCnpjWithNewAlphanumericFormat() {
        PessoaJuridica pessoa = new PessoaJuridica("Empresa", "e@e.com", "$hash", "ABCDEF12345612");

        assertThat(pessoa.getCnpj()).isEqualTo("ABCDEF12345612");
    }

    @Test
    @DisplayName("Should throw InvalidCnpjException when CNPJ has 13 characters")
    void shouldThrowInvalidCnpjExceptionWhenCnpjHas13Characters() {
        assertThatThrownBy(() -> new PessoaJuridica("Empresa", "e@e.com", "$hash", "1234567800019"))
                .isInstanceOf(InvalidCnpjException.class);
    }

    @Test
    @DisplayName("Should throw InvalidCnpjException when CNPJ has 15 characters")
    void shouldThrowInvalidCnpjExceptionWhenCnpjHas15Characters() {
        assertThatThrownBy(() -> new PessoaJuridica("Empresa", "e@e.com", "$hash", "123456780001901"))
                .isInstanceOf(InvalidCnpjException.class);
    }
}
