package com.inter.remessa.adapter.out.persistence;

import com.inter.remessa.domain.model.Cotacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface CotacaoJpaRepository extends JpaRepository<Cotacao, Long> {

    Optional<Cotacao> findFirstByOrderByDataDesc();
}
