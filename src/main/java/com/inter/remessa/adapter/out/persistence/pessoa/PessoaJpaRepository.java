package com.inter.remessa.adapter.out.persistence.pessoa;

import com.inter.remessa.domain.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

interface PessoaJpaRepository extends JpaRepository<Pessoa, Long> {
    boolean existsByEmail(String email);
}
