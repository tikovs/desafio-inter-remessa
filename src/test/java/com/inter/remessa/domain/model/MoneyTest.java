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
}
