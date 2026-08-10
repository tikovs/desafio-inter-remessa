package com.inter.remessa.adapter.in.web.remessa;

import java.math.BigDecimal;

public record RemessaRequest(Long remetenteId, Long destinatarioId, BigDecimal valor) {}
