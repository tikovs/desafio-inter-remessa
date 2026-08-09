package com.inter.remessa.adapter.in.web;

import java.math.BigDecimal;

public record RemessaRequest(Long remetenteId, Long destinatarioId, BigDecimal valor) {}
