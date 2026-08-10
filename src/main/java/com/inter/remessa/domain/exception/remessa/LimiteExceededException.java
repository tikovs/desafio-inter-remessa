package com.inter.remessa.domain.exception.remessa;

public class LimiteExceededException extends RuntimeException {
    public LimiteExceededException() {
        super("Limite diário de remessa excedido");
    }
}
