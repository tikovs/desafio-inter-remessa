package com.inter.remessa.application.usecase;

import com.inter.remessa.adapter.out.bcb.CotacaoBcbAdapter;
import com.inter.remessa.application.port.out.CotacaoProviderPort;
import com.inter.remessa.application.port.out.CotacaoRepositoryPort;
import com.inter.remessa.config.RedisConfig;
import com.inter.remessa.domain.exception.CotacaoIndisponiveException;
import com.inter.remessa.domain.model.Cotacao;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
public class CotacaoService implements CotacaoProviderPort {

    private static final int MAX_FALLBACK_DAYS = 10;

    private final CotacaoBcbAdapter bcbAdapter;
    private final CotacaoRepositoryPort cotacaoRepository;

    public CotacaoService(CotacaoBcbAdapter bcbAdapter, CotacaoRepositoryPort cotacaoRepository) {
        this.bcbAdapter = bcbAdapter;
        this.cotacaoRepository = cotacaoRepository;
    }

    @Override
    @Cacheable(value = RedisConfig.CACHE_COTACOES, key = "#date")
    public BigDecimal getCotacaoDolar(LocalDate date) {
        for (int i = 0; i <= MAX_FALLBACK_DAYS; i++) {
            LocalDate candidate = date.minusDays(i);
            Optional<BigDecimal> taxa = bcbAdapter.getCotacaoDolar(candidate);
            if (taxa.isPresent()) {
                cotacaoRepository.save(new Cotacao(candidate, taxa.get()));
                return taxa.get();
            }
        }
        throw new CotacaoIndisponiveException(date.toString());
    }
}
