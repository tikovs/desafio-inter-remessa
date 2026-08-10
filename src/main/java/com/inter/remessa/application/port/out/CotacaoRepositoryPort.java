package com.inter.remessa.application.port.out;

import com.inter.remessa.domain.model.Cotacao;

import java.time.LocalDate;
import java.util.Optional;

public interface CotacaoRepositoryPort {
    void save(Cotacao cotacao);
    Optional<Cotacao> findByData(LocalDate data);
}
