package com.inter.remessa.adapter.out.persistence.wallet;

import com.inter.remessa.application.port.out.WalletRepositoryPort;
import com.inter.remessa.domain.model.Wallet;
import org.springframework.stereotype.Repository;

@Repository
class WalletJpaAdapter implements WalletRepositoryPort {

    private final WalletJpaRepository repository;

    WalletJpaAdapter(WalletJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Wallet findByPessoaId(Long pessoaId) {
        return repository.findByPessoaId(pessoaId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet não encontrada para pessoaId=" + pessoaId));
    }

    @Override
    public void save(Wallet wallet) {
        repository.save(wallet);
    }
}
