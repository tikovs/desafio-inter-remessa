package com.inter.remessa.application.usecase;

import com.inter.remessa.adapter.out.bcb.CotacaoBcbAdapter;
import com.inter.remessa.application.port.out.CotacaoRepositoryPort;
import com.inter.remessa.domain.exception.CotacaoIndisponiveException;
import com.inter.remessa.domain.model.Cotacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CotacaoServiceTest {

    private CotacaoBcbAdapter bcbAdapter;
    private CotacaoRepositoryPort cotacaoRepository;
    private CotacaoService service;

    @BeforeEach
    void setUp() {
        bcbAdapter = mock(CotacaoBcbAdapter.class);
        cotacaoRepository = mock(CotacaoRepositoryPort.class);
        service = new CotacaoService(bcbAdapter, cotacaoRepository);
    }

    @Test
    @DisplayName("Should call BCB and save cotação when weekday returns data")
    void shouldCallBcbAndSaveCotacaoWhenWeekday() {
        LocalDate monday = LocalDate.of(2024, 3, 18);
        when(bcbAdapter.getCotacaoDolar(monday)).thenReturn(Optional.of(new BigDecimal("5.10")));

        BigDecimal result = service.getCotacaoDolar(monday);

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.10"));
        verify(bcbAdapter).getCotacaoDolar(monday);
        verify(cotacaoRepository).save(any(Cotacao.class));
    }

    @Test
    @DisplayName("Should walk back to Friday when BCB has no data for Saturday")
    void shouldWalkBackToFridayWhenSaturdayHasNoData() {
        LocalDate saturday = LocalDate.of(2024, 3, 16);
        LocalDate friday = LocalDate.of(2024, 3, 15);
        when(bcbAdapter.getCotacaoDolar(saturday)).thenReturn(Optional.empty());
        when(bcbAdapter.getCotacaoDolar(friday)).thenReturn(Optional.of(new BigDecimal("5.15")));

        BigDecimal result = service.getCotacaoDolar(saturday);

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.15"));
        verify(bcbAdapter).getCotacaoDolar(friday);
        verify(cotacaoRepository).save(any(Cotacao.class));
    }

    @Test
    @DisplayName("Should walk back two days to Friday when Sunday and Saturday both have no data")
    void shouldWalkBackTwoDaysToFridayWhenSundayAndSaturdayHaveNoData() {
        LocalDate sunday = LocalDate.of(2024, 3, 17);
        LocalDate saturday = LocalDate.of(2024, 3, 16);
        LocalDate friday = LocalDate.of(2024, 3, 15);
        when(bcbAdapter.getCotacaoDolar(sunday)).thenReturn(Optional.empty());
        when(bcbAdapter.getCotacaoDolar(saturday)).thenReturn(Optional.empty());
        when(bcbAdapter.getCotacaoDolar(friday)).thenReturn(Optional.of(new BigDecimal("5.15")));

        BigDecimal result = service.getCotacaoDolar(sunday);

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.15"));
        verify(bcbAdapter).getCotacaoDolar(friday);
        verify(cotacaoRepository).save(any(Cotacao.class));
    }

    @Test
    @DisplayName("Should walk back to last available day when weekday PTAX has not been published yet")
    void shouldWalkBackToPreviousDayWhenWeekdayHasNoDataYet() {
        LocalDate monday = LocalDate.of(2024, 3, 18);
        LocalDate friday = LocalDate.of(2024, 3, 15);
        when(bcbAdapter.getCotacaoDolar(any())).thenReturn(Optional.empty());
        when(bcbAdapter.getCotacaoDolar(friday)).thenReturn(Optional.of(new BigDecimal("5.00")));

        BigDecimal result = service.getCotacaoDolar(monday);

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.00"));
        verify(cotacaoRepository).save(any(Cotacao.class));
    }

    @Test
    @DisplayName("Should throw CotacaoIndisponiveException when all fallback days return no data")
    void shouldThrowExceptionWhenAllFallbackDaysEmpty() {
        LocalDate date = LocalDate.of(2024, 3, 18);
        when(bcbAdapter.getCotacaoDolar(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCotacaoDolar(date))
                .isInstanceOf(CotacaoIndisponiveException.class);
    }
}
