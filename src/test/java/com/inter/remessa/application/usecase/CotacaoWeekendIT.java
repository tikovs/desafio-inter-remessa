package com.inter.remessa.application.usecase;

import com.inter.remessa.adapter.out.bcb.CotacaoBcbAdapter;
import com.inter.remessa.application.port.out.CotacaoRepositoryPort;
import com.inter.remessa.domain.model.Cotacao;
import jakarta.persistence.EntityManager;
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

import com.inter.remessa.config.RedisConfig;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    @Autowired CotacaoRepositoryPort cotacaoRepository;
    @Autowired CotacaoBcbAdapter bcbAdapter;
    @Autowired CacheManager cacheManager;
    @Autowired EntityManager em;

    @BeforeEach
    void setUp() {
        Mockito.reset(bcbAdapter);
        cacheManager.getCache(RedisConfig.CACHE_COTACOES).clear();
    }

    @Test
    @DisplayName("Should return last saved cotação from database without calling BCB when requested on Saturday")
    void shouldReturnLastCotacaoFromDatabaseWithoutCallingBcbOnSaturday() {
        em.persist(new Cotacao(LocalDate.of(2024, 3, 15), new BigDecimal("5.1234")));
        em.flush();

        BigDecimal result = cotacaoService.getCotacaoDolar(LocalDate.of(2024, 3, 16)); // Saturday

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.1234"));
        verify(bcbAdapter, never()).getCotacaoDolar(any());
    }

    @Test
    @DisplayName("Should return last saved cotação from database without calling BCB when requested on Sunday")
    void shouldReturnLastCotacaoFromDatabaseWithoutCallingBcbOnSunday() {
        em.persist(new Cotacao(LocalDate.of(2024, 3, 15), new BigDecimal("5.0902")));
        em.flush();

        BigDecimal result = cotacaoService.getCotacaoDolar(LocalDate.of(2024, 3, 17)); // Sunday

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.0902"));
        verify(bcbAdapter, never()).getCotacaoDolar(any());
    }

    @Test
    @DisplayName("Should call BCB with nearest Friday when on weekend and database has no stored cotação")
    void shouldCallBcbWithNearestFridayOnWeekendWhenDatabaseIsEmpty() {
        LocalDate friday = LocalDate.of(2024, 3, 15);
        when(bcbAdapter.getCotacaoDolar(friday)).thenReturn(new BigDecimal("5.0500"));

        BigDecimal result = cotacaoService.getCotacaoDolar(LocalDate.of(2024, 3, 16)); // Saturday

        assertThat(result).isEqualByComparingTo(new BigDecimal("5.0500"));
        verify(bcbAdapter).getCotacaoDolar(friday);
    }
}
