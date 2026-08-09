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

    @Test
    @DisplayName("Should return difference when a smaller Money is subtracted from a larger one")
    void shouldReturnDifferenceWhenSmallerMoneyIsSubtracted() {
        Money result = Money.ofReais(new BigDecimal("15.75")).subtract(Money.ofReais(new BigDecimal("5.25")));
        assertThat(result).isEqualTo(Money.ofReais(new BigDecimal("10.50")));
    }

    @Test
    @DisplayName("Should round to nearest even cent (HALF_EVEN) when value has more than 2 decimal places")
    void shouldRoundToNearestEvenWhenValueHasMoreThanTwoDecimalPlaces() {
        // 10.505 = 1050.5 cents — HALF_EVEN rounds to 1050 (even), not 1051 (odd)
        Money money = Money.ofReais(new BigDecimal("10.505"));
        assertThat(money.cents()).isEqualTo(1050L);
    }
}
