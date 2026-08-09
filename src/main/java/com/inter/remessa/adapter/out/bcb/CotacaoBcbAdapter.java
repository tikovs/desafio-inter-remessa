package com.inter.remessa.adapter.out.bcb;

import com.inter.remessa.application.port.out.CotacaoProviderPort;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class CotacaoBcbAdapter implements CotacaoProviderPort {

    private static final DateTimeFormatter BCB_DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    private final RestClient restClient;

    public CotacaoBcbAdapter(RestClient bcbRestClient) {
        this.restClient = bcbRestClient;
    }

    @Override
    public BigDecimal getCotacaoDolar(LocalDate date) {
        String formattedDate = date.format(BCB_DATE_FORMAT);
        String path = "/CotacaoDolarDia(dataCotacao=@dataCotacao)?@dataCotacao='" + formattedDate + "'&$format=json";
        CotacaoBcbResponse response = restClient.get()
                .uri(path)
                .retrieve()
                .body(CotacaoBcbResponse.class);
        return response.value().get(0).cotacaoCompra();
    }
}
