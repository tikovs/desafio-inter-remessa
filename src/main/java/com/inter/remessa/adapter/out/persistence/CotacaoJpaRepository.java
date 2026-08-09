package com.inter.remessa.adapter.out.persistence;

import com.inter.remessa.domain.model.Cotacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

interface CotacaoJpaRepository extends JpaRepository<Cotacao, Long> {

    @Query("SELECT c FROM Cotacao c ORDER BY c.data DESC LIMIT 1")
    Optional<Cotacao> findLatest();
}
