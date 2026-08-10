package com.inter.remessa.adapter.out.bcb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CotacaoBcbAdapterTest {

    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestSpec;
    private RestClient.ResponseSpec responseSpec;
    private CotacaoBcbAdapter adapter;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        RestClient restClient = mock(RestClient.class);
        requestSpec = mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestSpec);
        doReturn(requestSpec).when(requestSpec).uri(anyString());
        when(requestSpec.retrieve()).thenReturn(responseSpec);

        adapter = new CotacaoBcbAdapter(restClient);
    }

    @Test
    @DisplayName("Should return cotacaoCompra extracted from BCB OData response value[0]")
    void shouldReturnCotacaoCompraFromBcbResponse() {
        CotacaoBcbResponse fakeResponse = new CotacaoBcbResponse(
                List.of(new CotacaoBcbResponse.CotacaoItem(new BigDecimal("4.9725")))
        );
        when(responseSpec.body(CotacaoBcbResponse.class)).thenReturn(fakeResponse);

        Optional<BigDecimal> result = adapter.getCotacaoDolar(LocalDate.of(2024, 1, 15));

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(new BigDecimal("4.9725"));
    }

    @Test
    @DisplayName("Should format request date as MM-DD-YYYY (American format required by BCB API)")
    void shouldFormatDateAsAmericanFormatInUri() {
        CotacaoBcbResponse fakeResponse = new CotacaoBcbResponse(
                List.of(new CotacaoBcbResponse.CotacaoItem(new BigDecimal("5.00")))
        );
        when(responseSpec.body(CotacaoBcbResponse.class)).thenReturn(fakeResponse);

        adapter.getCotacaoDolar(LocalDate.of(2024, 3, 5));

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).uri(uriCaptor.capture());
        assertThat(uriCaptor.getValue()).contains("03-05-2024");
    }

    @Test
    @DisplayName("Should return empty Optional when BCB returns empty array")
    void shouldReturnEmptyOptionalWhenBcbReturnsEmptyArray() {
        when(responseSpec.body(CotacaoBcbResponse.class))
                .thenReturn(new CotacaoBcbResponse(Collections.emptyList()));

        Optional<BigDecimal> result = adapter.getCotacaoDolar(LocalDate.of(2024, 3, 15));

        assertThat(result).isEmpty();
    }
}
