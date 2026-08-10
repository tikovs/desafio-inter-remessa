package com.inter.remessa.adapter.out.persistence.pessoa;

import com.inter.remessa.domain.model.PessoaJuridica;
import org.springframework.data.jpa.repository.JpaRepository;

interface PessoaJuridicaJpaRepository extends JpaRepository<PessoaJuridica, Long> {
    boolean existsByCnpj(String cnpj);
}
