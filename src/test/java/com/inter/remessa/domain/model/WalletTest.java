package com.inter.remessa.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WalletTest {

    @Test
    @DisplayName("Should start with zero balance in both BRL and USD when a new Wallet is created")
    void shouldStartWithZeroBalanceWhenNewWalletIsCreated() {
        Wallet wallet = new Wallet();

        assertThat(wallet.getSaldoBrl()).isEqualTo(Money.ofCents(0));
        assertThat(wallet.getSaldoUsd()).isEqualTo(Money.ofCents(0));
    }
}
