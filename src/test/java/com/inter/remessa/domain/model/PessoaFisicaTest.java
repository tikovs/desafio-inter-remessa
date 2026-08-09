package com.inter.remessa.domain.model;

import com.inter.remessa.domain.exception.CpfInvalidoException;
import com.inter.remessa.domain.exception.EmailInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PessoaFisicaTest {

    @Test
    @DisplayName("Should store name, email, password and CPF and return FISICA type when created with valid data")
    void shouldStoreFieldsAndReturnFisicaTypeWhenCreatedWithValidData() {
        PessoaFisica pessoa = new PessoaFisica("João Silva", "joao@email.com", "$2a$10$hashedpassword", "12345678909");

        assertThat(pessoa.getNome()).isEqualTo("João Silva");
        assertThat(pessoa.getEmail()).isEqualTo("joao@email.com");
        assertThat(pessoa.getSenhaHash()).isEqualTo("$2a$10$hashedpassword");
        assertThat(pessoa.getCpf()).isEqualTo("12345678909");
        assertThat(pessoa.getTipo()).isEqualTo(TipoPessoa.FISICA);
    }

    @Test
    @DisplayName("Should throw CpfInvalidoException when CPF does not match 11 digits")
    void shouldThrowCpfInvalidoExceptionWhenCpfIsInvalid() {
        assertThatThrownBy(() -> new PessoaFisica("João Silva", "joao@email.com", "$hash", "123.456.789-09"))
                .isInstanceOf(CpfInvalidoException.class);
    }

    @Test
    @DisplayName("Should throw EmailInvalidoException when email has invalid format")
    void shouldThrowEmailInvalidoExceptionWhenEmailIsInvalid() {
        assertThatThrownBy(() -> new PessoaFisica("João Silva", "email-invalido", "$hash", "12345678909"))
                .isInstanceOf(EmailInvalidoException.class);
    }

    @Test
    @DisplayName("Should store email in lowercase when created with uppercase characters")
    void shouldStoreEmailInLowercaseWhenCreatedWithUppercaseCharacters() {
        PessoaFisica pessoa = new PessoaFisica("João Silva", "Joao@Email.COM", "$hash", "12345678909");

        assertThat(pessoa.getEmail()).isEqualTo("joao@email.com");
    }
}
