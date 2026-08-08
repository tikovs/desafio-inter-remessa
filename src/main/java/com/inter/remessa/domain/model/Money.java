package com.inter.remessa.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(long cents) {

    public static Money ofReais(BigDecimal reais) {
        long centavos = reais.movePointRight(2).setScale(0, RoundingMode.HALF_EVEN).longValueExact();
        return new Money(centavos);
    }
}
