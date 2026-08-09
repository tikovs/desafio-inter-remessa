package com.inter.remessa.domain.model;

import com.inter.remessa.adapter.out.persistence.MoneyConverter;
import jakarta.persistence.*;

@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    @Convert(converter = MoneyConverter.class)
    @Column(name = "reais_balance_cents", nullable = false)
    private Money reaisBalance;

    @Convert(converter = MoneyConverter.class)
    @Column(name = "dollars_balance_cents", nullable = false)
    private Money dollarsBalance;

    public Wallet() {
        this.reaisBalance = Money.ofCents(0);
        this.dollarsBalance = Money.ofCents(0);
    }

    public Wallet(Pessoa pessoa) {
        this.pessoa = pessoa;
        this.reaisBalance = Money.ofCents(0);
        this.dollarsBalance = Money.ofCents(0);
    }

    public Wallet(Pessoa pessoa, Money initialReaisBalance) {
        this.pessoa = pessoa;
        this.reaisBalance = initialReaisBalance;
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

    public Long getId() { return id; }
    public Pessoa getPessoa() { return pessoa; }
    public Money getBalanceBrl() { return reaisBalance; }
    public Money getBalanceUsd() { return dollarsBalance; }
}
