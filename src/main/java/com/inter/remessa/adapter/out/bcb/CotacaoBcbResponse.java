package com.inter.remessa.adapter.out.bcb;

import java.math.BigDecimal;
import java.util.List;

record CotacaoBcbResponse(List<CotacaoItem> value) {
    record CotacaoItem(BigDecimal cotacaoCompra) {}
}
