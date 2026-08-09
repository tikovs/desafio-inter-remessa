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
    @DisplayName("Should call BCB and save cotação to database when requested on a weekday")
    void shouldCallBcbAndSaveCotacaoWhenWeekday() {
        LocalDate monday = LocalDate.of(2024, 3, 18);
        when(bcbAdapter.getCotacaoDolar(monday)).thenReturn(new BigDecimal("5.10"));

        BigDecimal result = service.getCotacaoDolar(monday);

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.10"));
        verify(bcbAdapter).getCotacaoDolar(monday);
        verify(cotacaoRepository).save(any(Cotacao.class));
    }

    @Test
    @DisplayName("Should return last cotação from database without calling BCB when requested on Saturday")
    void shouldReturnLastCotacaoFromDatabaseWithoutCallingBcbOnSaturday() {
        LocalDate saturday = LocalDate.of(2024, 3, 16);
        Cotacao lastCotacao = new Cotacao(LocalDate.of(2024, 3, 15), new BigDecimal("5.05"));
        when(cotacaoRepository.findLatest()).thenReturn(Optional.of(lastCotacao));

        BigDecimal result = service.getCotacaoDolar(saturday);

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.05"));
        verify(bcbAdapter, never()).getCotacaoDolar(any());
    }

    @Test
    @DisplayName("Should return last cotação from database without calling BCB when requested on Sunday")
    void shouldReturnLastCotacaoFromDatabaseWithoutCallingBcbOnSunday() {
        LocalDate sunday = LocalDate.of(2024, 3, 17);
        Cotacao lastCotacao = new Cotacao(LocalDate.of(2024, 3, 15), new BigDecimal("5.20"));
        when(cotacaoRepository.findLatest()).thenReturn(Optional.of(lastCotacao));

        BigDecimal result = service.getCotacaoDolar(sunday);

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.20"));
        verify(bcbAdapter, never()).getCotacaoDolar(any());
    }

    @Test
    @DisplayName("Should call BCB with nearest Friday when on Saturday and database is empty")
    void shouldCallBcbWithNearestFridayOnSaturdayWhenDatabaseIsEmpty() {
        LocalDate saturday = LocalDate.of(2024, 3, 16);
        LocalDate friday = LocalDate.of(2024, 3, 15);
        when(cotacaoRepository.findLatest()).thenReturn(Optional.empty());
        when(bcbAdapter.getCotacaoDolar(friday)).thenReturn(new BigDecimal("5.15"));

        BigDecimal result = service.getCotacaoDolar(saturday);

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.15"));
        verify(bcbAdapter).getCotacaoDolar(friday);
        verify(cotacaoRepository).save(any(Cotacao.class));
    }

    @Test
    @DisplayName("Should call BCB with nearest Friday when on Sunday and database is empty")
    void shouldCallBcbWithNearestFridayOnSundayWhenDatabaseIsEmpty() {
        LocalDate sunday = LocalDate.of(2024, 3, 17);
        LocalDate friday = LocalDate.of(2024, 3, 15);
        when(cotacaoRepository.findLatest()).thenReturn(Optional.empty());
        when(bcbAdapter.getCotacaoDolar(friday)).thenReturn(new BigDecimal("5.15"));

        BigDecimal result = service.getCotacaoDolar(sunday);

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.15"));
        verify(bcbAdapter).getCotacaoDolar(friday);
        verify(cotacaoRepository).save(any(Cotacao.class));
    }
}
