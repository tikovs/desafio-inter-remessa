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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

@Service
@Transactional
public class CotacaoService implements CotacaoProviderPort {

    private final CotacaoBcbAdapter bcbAdapter;
    private final CotacaoRepositoryPort cotacaoRepository;

    public CotacaoService(CotacaoBcbAdapter bcbAdapter, CotacaoRepositoryPort cotacaoRepository) {
        this.bcbAdapter = bcbAdapter;
        this.cotacaoRepository = cotacaoRepository;
    }

    @Override
    @Cacheable(value = RedisConfig.CACHE_COTACOES, key = "#date")
    public BigDecimal getCotacaoDolar(LocalDate date) {
        if (isWeekend(date)) {
            return getCotacaoParaFimDeSemana(date);
        }
        return fetchFromBcbAndSave(date);
    }

    private BigDecimal getCotacaoParaFimDeSemana(LocalDate date) {
        Optional<Cotacao> saved = cotacaoRepository.findLatest();
        if (saved.isPresent()) {
            return saved.get().getTaxa();
        }
        LocalDate friday = date.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        return fetchFromBcbAndSave(friday);
    }

    private BigDecimal fetchFromBcbAndSave(LocalDate date) {
        BigDecimal taxa = bcbAdapter.getCotacaoDolar(date);
        cotacaoRepository.save(new Cotacao(date, taxa));
        return taxa;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
