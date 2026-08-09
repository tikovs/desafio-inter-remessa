package com.inter.remessa.domain.model;

public class Wallet {

    private Money reaisBalance;
    private Money dollarsBalance;

    public Wallet() {
        this.reaisBalance = Money.ofCents(0);
        this.dollarsBalance = Money.ofCents(0);
    }

    public Wallet(Money initialReaisBalance) {
        this.reaisBalance = initialReaisBalance;
        this.dollarsBalance = Money.ofCents(0);
    }

    public void debitReais(Money amount) {
        this.reaisBalance = this.reaisBalance.subtract(amount);
    }

    public void creditDollars(Money amount) {
        this.dollarsBalance = this.dollarsBalance.add(amount);
    }

    public Money getBalanceBrl() { return reaisBalance; }
    public Money getBalanceUsd() { return dollarsBalance; }
}
