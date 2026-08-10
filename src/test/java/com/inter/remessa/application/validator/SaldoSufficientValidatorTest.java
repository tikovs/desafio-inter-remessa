package com.inter.remessa.application.validator;

import com.inter.remessa.domain.exception.remessa.SaldoInsufficientException;
import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.TipoPessoa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaldoSufficientValidatorTest {

    private final SaldoSufficientValidator validator = new SaldoSufficientValidator();

    @Test
    @DisplayName("Should not throw when wallet balance is greater than the remessa amount")
    void shouldNotThrowWhenBalanceCoversAmount() {
        RemessaValidationContext ctx = new RemessaValidationContext(
                Money.ofReais(new BigDecimal("100.00")),
                Money.ofReais(new BigDecimal("500.00")),
                Money.ofCents(0),
                TipoPessoa.FISICA
        );

        assertThatNoException().isThrownBy(() -> validator.validate(ctx));
    }

    @Test
    @DisplayName("Should throw SaldoInsufficientException when balance is less than the remessa amount")
    void shouldThrowWhenBalanceIsInsufficient() {
        RemessaValidationContext ctx = new RemessaValidationContext(
                Money.ofReais(new BigDecimal("100.00")),
                Money.ofReais(new BigDecimal("50.00")),
                Money.ofCents(0),
                TipoPessoa.FISICA
        );

        assertThatThrownBy(() -> validator.validate(ctx))
                .isInstanceOf(SaldoInsufficientException.class);
    }
}
