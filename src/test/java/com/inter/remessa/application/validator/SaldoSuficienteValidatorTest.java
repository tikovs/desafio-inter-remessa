package com.inter.remessa.application.validator;

import com.inter.remessa.domain.exception.SaldoInsuficienteException;
import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.TipoPessoa;
import com.inter.remessa.domain.model.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaldoSuficienteValidatorTest {

    private final SaldoSuficienteValidator validator = new SaldoSuficienteValidator();

    @Test
    @DisplayName("Should not throw when wallet balance is greater than the remessa amount")
    void shouldNotThrowWhenBalanceCoversAmount() {
        Wallet wallet = new Wallet(Money.ofReais(new BigDecimal("500.00")));
        ValidacaoRemessaContext ctx = new ValidacaoRemessaContext(
                Money.ofReais(new BigDecimal("100.00")),
                wallet,
                Money.ofCents(0),
                TipoPessoa.FISICA
        );

        assertThatNoException().isThrownBy(() -> validator.validate(ctx));
    }

    @Test
    @DisplayName("Should throw SaldoInsuficienteException when balance is less than the remessa amount")
    void shouldThrowWhenBalanceIsInsufficient() {
        Wallet wallet = new Wallet(Money.ofReais(new BigDecimal("50.00")));
        ValidacaoRemessaContext ctx = new ValidacaoRemessaContext(
                Money.ofReais(new BigDecimal("100.00")),
                wallet,
                Money.ofCents(0),
                TipoPessoa.FISICA
        );

        assertThatThrownBy(() -> validator.validate(ctx))
                .isInstanceOf(SaldoInsuficienteException.class);
    }
}
