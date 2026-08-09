package com.inter.remessa.domain.exception;

public class CotacaoIndisponiveException extends RuntimeException {
    public CotacaoIndisponiveException(String date) {
        super("Cotação do dólar indisponível para a data " + date + ". Tente novamente mais tarde.");
    }
}
