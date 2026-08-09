package com.inter.remessa.adapter.out.bcb;

import com.inter.remessa.domain.exception.CotacaoIndisponiveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        BigDecimal result = adapter.getCotacaoDolar(LocalDate.of(2024, 1, 15));

        assertThat(result).isEqualByComparingTo(new BigDecimal("4.9725"));
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
        //noinspection unchecked
        verify(requestSpec).uri(uriCaptor.capture());
        assertThat(uriCaptor.getValue()).contains("03-05-2024");
    }

    @Test
    @DisplayName("Should return cotação on first attempt when requested date has data available")
    void shouldReturnCotacaoOnFirstAttemptWhenDateHasData() {
        CotacaoBcbResponse response = new CotacaoBcbResponse(
                List.of(new CotacaoBcbResponse.CotacaoItem(new BigDecimal("5.10")))
        );
        when(responseSpec.body(CotacaoBcbResponse.class)).thenReturn(response);

        BigDecimal result = adapter.getCotacaoDolar(LocalDate.of(2024, 3, 18));

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.10"));
        //noinspection unchecked
        verify(requestSpec, times(1)).uri(anyString());
    }

    @Test
    @DisplayName("Should return previous day cotação when requested date has no data")
    void shouldReturnPreviousDayCotacaoWhenRequestedDateHasNoData() {
        CotacaoBcbResponse empty = new CotacaoBcbResponse(Collections.emptyList());
        CotacaoBcbResponse withRate = new CotacaoBcbResponse(
                List.of(new CotacaoBcbResponse.CotacaoItem(new BigDecimal("5.05")))
        );
        when(responseSpec.body(CotacaoBcbResponse.class)).thenReturn(empty, withRate);

        BigDecimal result = adapter.getCotacaoDolar(LocalDate.of(2024, 3, 19));

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.05"));
        //noinspection unchecked
        verify(requestSpec, times(2)).uri(anyString());
    }

    @Test
    @DisplayName("Should walk back multiple days until finding cotação when Saturday and Sunday have no data")
    void shouldWalkBackMultipleDaysWhenConsecutiveDatesHaveNoCotacao() {
        CotacaoBcbResponse empty = new CotacaoBcbResponse(Collections.emptyList());
        CotacaoBcbResponse friday = new CotacaoBcbResponse(
                List.of(new CotacaoBcbResponse.CotacaoItem(new BigDecimal("5.00")))
        );
        // Sunday → empty, Saturday → empty, Friday → has data
        when(responseSpec.body(CotacaoBcbResponse.class)).thenReturn(empty, empty, friday);

        BigDecimal result = adapter.getCotacaoDolar(LocalDate.of(2024, 3, 17)); // Sunday

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.00"));
        //noinspection unchecked
        verify(requestSpec, times(3)).uri(anyString());
    }

    @Test
    @DisplayName("Should throw CotacaoIndisponiveException after 7 consecutive days without data")
    void shouldThrowCotacaoIndisponiveExceptionAfterSevenFailedAttempts() {
        CotacaoBcbResponse empty = new CotacaoBcbResponse(Collections.emptyList());
        when(responseSpec.body(CotacaoBcbResponse.class)).thenReturn(
                empty, empty, empty, empty, empty, empty, empty
        );

        assertThatThrownBy(() -> adapter.getCotacaoDolar(LocalDate.of(2024, 3, 17)))
                .isInstanceOf(CotacaoIndisponiveException.class);
        //noinspection unchecked
        verify(requestSpec, times(7)).uri(anyString());
    }
}
