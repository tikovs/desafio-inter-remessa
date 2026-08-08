package com.inter.remessa.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @Test
    @DisplayName("Should convert reais value to cents")
    void shouldConvertReaisValueToCentsWhenOfReaisIsCalled() {
        Money money = Money.ofReais(new BigDecimal("10.50"));
        assertThat(money.cents()).isEqualTo(1050L);
    }

    @Test
    @DisplayName("Should return sum of both amounts when two Money values are added")
    void shouldReturnSumWhenTwoMoneyValuesAreAdded() {
        Money result = Money.ofReais(new BigDecimal("10.50")).add(Money.ofReais(new BigDecimal("5.25")));
        assertThat(result).isEqualTo(Money.ofReais(new BigDecimal("15.75")));
    }
}
