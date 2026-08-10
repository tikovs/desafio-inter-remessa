package com.inter.remessa.application.usecase;

import com.inter.remessa.adapter.out.bcb.CotacaoBcbAdapter;
import com.inter.remessa.config.RedisConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
@ActiveProfiles("test")
class CotacaoWeekendIT {

    @TestConfiguration
    static class StubBcb {
        @Bean
        @Primary
        CotacaoBcbAdapter bcbAdapter() {
            return mock(CotacaoBcbAdapter.class);
        }
    }

    @Autowired CotacaoService cotacaoService;
    @Autowired CotacaoBcbAdapter bcbAdapter;
    @Autowired CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Mockito.reset(bcbAdapter);
        cacheManager.getCache(RedisConfig.CACHE_COTACOES).clear();
    }

    @Test
    @DisplayName("Should walk back to Friday when requested on Saturday and BCB has no Saturday data")
    void shouldReturnFridayRateWhenRequestedOnSaturday() {
        LocalDate saturday = LocalDate.of(2024, 3, 16);
        LocalDate friday = LocalDate.of(2024, 3, 15);
        when(bcbAdapter.getCotacaoDolar(saturday)).thenReturn(Optional.empty());
        when(bcbAdapter.getCotacaoDolar(friday)).thenReturn(Optional.of(new BigDecimal("5.1234")));

        BigDecimal result = cotacaoService.getCotacaoDolar(saturday);

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.1234"));
        verify(bcbAdapter).getCotacaoDolar(friday);
    }

    @Test
    @DisplayName("Should walk back to Friday when requested on Sunday and BCB has no weekend data")
    void shouldReturnFridayRateWhenRequestedOnSunday() {
        LocalDate sunday = LocalDate.of(2024, 3, 17);
        LocalDate saturday = LocalDate.of(2024, 3, 16);
        LocalDate friday = LocalDate.of(2024, 3, 15);
        when(bcbAdapter.getCotacaoDolar(sunday)).thenReturn(Optional.empty());
        when(bcbAdapter.getCotacaoDolar(saturday)).thenReturn(Optional.empty());
        when(bcbAdapter.getCotacaoDolar(friday)).thenReturn(Optional.of(new BigDecimal("5.0902")));

        BigDecimal result = cotacaoService.getCotacaoDolar(sunday);

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.0902"));
        verify(bcbAdapter).getCotacaoDolar(friday);
    }

    @Test
    @DisplayName("Should walk back to nearest available day when weekday PTAX has not been published yet")
    void shouldWalkBackWhenWeekdayHasNoDataYet() {
        LocalDate monday = LocalDate.of(2024, 3, 18);
        LocalDate friday = LocalDate.of(2024, 3, 15);
        when(bcbAdapter.getCotacaoDolar(monday)).thenReturn(Optional.empty());
        when(bcbAdapter.getCotacaoDolar(monday.minusDays(1))).thenReturn(Optional.empty()); // Sunday
        when(bcbAdapter.getCotacaoDolar(monday.minusDays(2))).thenReturn(Optional.empty()); // Saturday
        when(bcbAdapter.getCotacaoDolar(friday)).thenReturn(Optional.of(new BigDecimal("5.0500")));

        BigDecimal result = cotacaoService.getCotacaoDolar(monday);

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.0500"));
    }
}
