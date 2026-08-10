package com.inter.remessa.adapter.out.persistence.remessa;

import com.inter.remessa.domain.model.Remessa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

interface RemessaJpaRepository extends JpaRepository<Remessa, Long> {

    @Query("SELECT COALESCE(SUM(r.valorReais), 0) FROM Remessa r " +
           "WHERE r.remetente.id = :pessoaId " +
           "AND r.dataHora >= :startOfDay AND r.dataHora < :endOfDay")
    long sumValorReaisCentsByRemetenteAndDay(
            @Param("pessoaId") Long pessoaId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
