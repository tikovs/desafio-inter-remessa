package com.inter.remessa.application.validator;

import com.inter.remessa.domain.exception.remessa.LimiteExceededException;
import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.TipoPessoa;
import com.inter.remessa.domain.model.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LimiteDailyValidatorTest {

    private final LimiteDailyValidator validator = new LimiteDailyValidator();

    @Test
    @DisplayName("Should not throw when PF daily total plus amount stays within 10k limit")
    void shouldNotThrowWhenPfTotalIsWithinLimit() {
        Wallet wallet = new Wallet(Money.ofReais(new BigDecimal("20000.00")));
        RemessaValidationContext ctx = new RemessaValidationContext(
                Money.ofReais(new BigDecimal("3000.00")),
                wallet,
                Money.ofReais(new BigDecimal("6000.00")),
                TipoPessoa.FISICA
        );

        assertThatNoException().isThrownBy(() -> validator.validate(ctx));
    }

    @Test
    @DisplayName("Should throw LimiteExceededException when PF daily total plus amount exceeds 10k")
    void shouldThrowWhenPfExceedsDailyLimit() {
        Wallet wallet = new Wallet(Money.ofReais(new BigDecimal("20000.00")));
        RemessaValidationContext ctx = new RemessaValidationContext(
                Money.ofReais(new BigDecimal("5000.00")),
                wallet,
                Money.ofReais(new BigDecimal("6000.00")),
                TipoPessoa.FISICA
        );

        assertThatThrownBy(() -> validator.validate(ctx))
                .isInstanceOf(LimiteExceededException.class);
    }

    @Test
    @DisplayName("Should not throw when PJ daily total plus amount stays within 50k limit")
    void shouldNotThrowWhenPjTotalIsWithinLimit() {
        Wallet wallet = new Wallet(Money.ofReais(new BigDecimal("100000.00")));
        RemessaValidationContext ctx = new RemessaValidationContext(
                Money.ofReais(new BigDecimal("20000.00")),
                wallet,
                Money.ofReais(new BigDecimal("20000.00")),
                TipoPessoa.JURIDICA
        );

        assertThatNoException().isThrownBy(() -> validator.validate(ctx));
    }

    @Test
    @DisplayName("Should throw LimiteExceededException when PJ daily total plus amount exceeds 50k")
    void shouldThrowWhenPjExceedsDailyLimit() {
        Wallet wallet = new Wallet(Money.ofReais(new BigDecimal("100000.00")));
        RemessaValidationContext ctx = new RemessaValidationContext(
                Money.ofReais(new BigDecimal("20000.00")),
                wallet,
                Money.ofReais(new BigDecimal("40000.00")),
                TipoPessoa.JURIDICA
        );

        assertThatThrownBy(() -> validator.validate(ctx))
                .isInstanceOf(LimiteExceededException.class);
    }
}
