package com.inter.remessa.adapter.in.web;

import java.math.BigDecimal;

public record RemessaResponse(
        Long id,
        BigDecimal valorReais,
        BigDecimal valorDolares,
        BigDecimal cotacaoUtilizada,
        String status
) {}
