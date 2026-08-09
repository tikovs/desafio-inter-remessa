package com.inter.remessa.application.usecase;

import java.math.BigDecimal;

public record RealizarRemessaCommand(Long remetenteId, Long destinatarioId, BigDecimal valorReais) {}
