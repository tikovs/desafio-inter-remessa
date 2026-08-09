package com.inter.remessa.domain.model;

public class Wallet {

    private Money balanceBrl;
    private Money balanceUsd;

    public Wallet() {
        this.balanceBrl = Money.ofCents(0);
        this.balanceUsd = Money.ofCents(0);
    }

    public Money getBalanceBrl() { return balanceBrl; }
    public Money getBalanceUsd() { return balanceUsd; }
}
