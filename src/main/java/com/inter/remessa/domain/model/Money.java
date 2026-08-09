package com.inter.remessa.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(long cents) {

    public static Money ofCents(long cents) {
        return new Money(cents);
    }

    public static Money ofReais(BigDecimal reais) {
        long centavos = reais.movePointRight(2).setScale(0, RoundingMode.HALF_EVEN).longValueExact();
        return new Money(centavos);
    }

    public Money add(Money other) {
        return new Money(this.cents + other.cents);
    }

    public Money subtract(Money other) {
        return new Money(this.cents - other.cents);
    }

    public boolean isLessThan(Money other) {
        return this.cents < other.cents;
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(this.cents, 2);
    }

    public Money convert(BigDecimal exchangeRate) {
        BigDecimal reais = BigDecimal.valueOf(this.cents, 2);
        BigDecimal dollars = reais.divide(exchangeRate, 2, RoundingMode.HALF_EVEN);
        return Money.ofReais(dollars);
    }
}
