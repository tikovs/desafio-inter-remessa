package com.inter.remessa.adapter.out.persistence.pessoa;

import com.inter.remessa.domain.model.PessoaFisica;
import org.springframework.data.jpa.repository.JpaRepository;

interface PessoaFisicaJpaRepository extends JpaRepository<PessoaFisica, Long> {
    boolean existsByCpf(String cpf);
}
