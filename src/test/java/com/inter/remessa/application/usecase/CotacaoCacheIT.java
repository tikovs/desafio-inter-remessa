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
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.cache.type=simple") // sobrescreve none→simple para ativar @Cacheable
@Transactional
class CotacaoCacheIT {

    @TestConfiguration
    static class StubBcb {
        @Bean
        @Primary
        CotacaoBcbAdapter bcbAdapter() {
            return Mockito.mock(CotacaoBcbAdapter.class);
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
    @DisplayName("Should call BCB only once for the same date when cache is hit on second call")
    void shouldCallBcbOnlyOnceWhenCacheIsHitForSameDate() {
        LocalDate friday = LocalDate.of(2024, 3, 15);
        when(bcbAdapter.getCotacaoDolar(friday)).thenReturn(new BigDecimal("5.1234"));

        cotacaoService.getCotacaoDolar(friday);
        cotacaoService.getCotacaoDolar(friday);

        verify(bcbAdapter, times(1)).getCotacaoDolar(friday);
    }

    @Test
    @DisplayName("Should return the same rate on second call without calling BCB again")
    void shouldReturnCachedRateOnSecondCall() {
        LocalDate friday = LocalDate.of(2024, 3, 15);
        when(bcbAdapter.getCotacaoDolar(friday)).thenReturn(new BigDecimal("5.1234"));

        BigDecimal first = cotacaoService.getCotacaoDolar(friday);
        BigDecimal second = cotacaoService.getCotacaoDolar(friday);

        assertThat(second).isEqualByComparingTo(first);
        verify(bcbAdapter, times(1)).getCotacaoDolar(any());
    }

    @Test
    @DisplayName("Should call BCB separately for each different weekday date")
    void shouldCallBcbForEachDifferentDate() {
        LocalDate monday = LocalDate.of(2024, 3, 18);
        LocalDate tuesday = LocalDate.of(2024, 3, 19);
        when(bcbAdapter.getCotacaoDolar(monday)).thenReturn(new BigDecimal("5.10"));
        when(bcbAdapter.getCotacaoDolar(tuesday)).thenReturn(new BigDecimal("5.20"));

        cotacaoService.getCotacaoDolar(monday);
        cotacaoService.getCotacaoDolar(tuesday);

        verify(bcbAdapter, times(1)).getCotacaoDolar(monday);
        verify(bcbAdapter, times(1)).getCotacaoDolar(tuesday);
    }

    @Test
    @DisplayName("Should cache weekend rate and not call BCB on second weekend request")
    void shouldCacheWeekendRateOnSecondCall() {
        LocalDate friday = LocalDate.of(2024, 3, 15);
        LocalDate saturday = LocalDate.of(2024, 3, 16);
        when(bcbAdapter.getCotacaoDolar(friday)).thenReturn(new BigDecimal("5.0500"));

        cotacaoService.getCotacaoDolar(saturday); // miss — chama BCB com sexta
        cotacaoService.getCotacaoDolar(saturday); // hit — não chama BCB

        verify(bcbAdapter, times(1)).getCotacaoDolar(friday);
    }
}
