package com.inter.remessa.adapter.out.persistence;

import com.inter.remessa.application.port.out.CotacaoRepositoryPort;
import com.inter.remessa.domain.model.Cotacao;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class CotacaoJpaAdapter implements CotacaoRepositoryPort {

    private final CotacaoJpaRepository repository;

    CotacaoJpaAdapter(CotacaoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Cotacao cotacao) {
        repository.save(cotacao);
    }

    @Override
    public Optional<Cotacao> findLatest() {
        return repository.findLatest();
    }
}
