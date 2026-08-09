package com.inter.remessa.adapter.out.persistence;

import com.inter.remessa.domain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface WalletJpaRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByPessoaId(Long pessoaId);
}
