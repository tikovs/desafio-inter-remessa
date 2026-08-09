package com.inter.remessa.application.port.out;

import com.inter.remessa.domain.model.Wallet;

public interface WalletRepositoryPort {
    Wallet findByPessoaId(Long pessoaId);
    void save(Wallet wallet);
}
