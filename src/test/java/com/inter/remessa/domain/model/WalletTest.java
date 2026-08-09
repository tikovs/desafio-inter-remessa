package com.inter.remessa.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class WalletTest {

    @Test
    @DisplayName("Should start with zero balance in both BRL and USD when a new Wallet is created")
    void shouldStartWithZeroBalanceWhenNewWalletIsCreated() {
        Wallet wallet = new Wallet();

        assertThat(wallet.getBalanceBrl()).isEqualTo(Money.ofCents(0));
        assertThat(wallet.getBalanceUsd()).isEqualTo(Money.ofCents(0));
    }

    @Test
    @DisplayName("Should decrease BRL balance by debited amount when debitReais is called")
    void shouldDecreaseBrlBalanceWhenDebitReaisIsCalled() {
        Wallet wallet = new Wallet(Money.ofReais(new BigDecimal("100.00")));

        wallet.debitReais(Money.ofReais(new BigDecimal("30.00")));

        assertThat(wallet.getBalanceBrl()).isEqualTo(Money.ofReais(new BigDecimal("70.00")));
    }

    @Test
    @DisplayName("Should increase USD balance by credited amount when creditDollars is called")
    void shouldIncreaseUsdBalanceWhenCreditDollarsIsCalled() {
        Wallet wallet = new Wallet();

        wallet.creditDollars(Money.ofReais(new BigDecimal("50.00")));

        assertThat(wallet.getBalanceUsd()).isEqualTo(Money.ofReais(new BigDecimal("50.00")));
    }
}
