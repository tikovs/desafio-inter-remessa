package com.inter.remessa.domain.exception.remessa;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(Long pessoaId) {
        super("Wallet not found for pessoaId=" + pessoaId);
    }
}
