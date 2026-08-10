package com.inter.remessa.domain.exception.remessa;

public class SaldoInsufficientException extends RuntimeException {
    public SaldoInsufficientException() {
        super("Saldo insuficiente para realizar a remessa");
    }
}
