package com.inter.remessa.adapter.out.persistence.remessa;

import com.inter.remessa.application.port.out.RemessaRepositoryPort;
import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.Remessa;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
class RemessaJpaAdapter implements RemessaRepositoryPort {

    private final RemessaJpaRepository repository;

    RemessaJpaAdapter(RemessaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Remessa save(Remessa remessa) {
        return repository.save(remessa);
    }

    @Override
    public Money totalRemessasHoje(Long pessoaId, LocalDate date) {
        long cents = repository.sumValorReaisCentsByRemetenteAndDay(
                pessoaId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        );
        return Money.ofCents(cents);
    }
}
