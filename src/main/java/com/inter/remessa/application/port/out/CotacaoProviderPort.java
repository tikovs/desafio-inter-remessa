package com.inter.remessa.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CotacaoProviderPort {
    BigDecimal getCotacaoDolar(LocalDate date);
}
