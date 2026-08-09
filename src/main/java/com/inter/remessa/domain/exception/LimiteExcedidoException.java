package com.inter.remessa.domain.exception;

public class LimiteExcedidoException extends RuntimeException {
    public LimiteExcedidoException() {
        super("Limite diário de remessa excedido");
    }
}
