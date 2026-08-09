package com.inter.remessa.adapter.out.bcb;

import com.inter.remessa.domain.exception.CotacaoIndisponiveException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CotacaoBcbAdapter {

    private static final DateTimeFormatter BCB_DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    private static final int MAX_LOOKBACK_DAYS = 7;

    private final RestClient restClient;

    public CotacaoBcbAdapter(RestClient bcbRestClient) {
        this.restClient = bcbRestClient;
    }

    public BigDecimal getCotacaoDolar(LocalDate date) {
        for (int i = 0; i < MAX_LOOKBACK_DAYS; i++) {
            LocalDate candidate = date.minusDays(i);
            String formattedDate = candidate.format(BCB_DATE_FORMAT);
            String path = "/CotacaoDolarDia(dataCotacao=@dataCotacao)?@dataCotacao='" + formattedDate + "'&$format=json";
            CotacaoBcbResponse response = restClient.get()
                    .uri(path)
                    .retrieve()
                    .body(CotacaoBcbResponse.class);
            List<CotacaoBcbResponse.CotacaoItem> items = (response != null) ? response.value() : List.of();
            if (items != null && !items.isEmpty()) {
                return items.get(0).cotacaoCompra();
            }
        }
        throw new CotacaoIndisponiveException(date.format(BCB_DATE_FORMAT));
    }
}
