package com.inter.remessa.domain.exception.cotacao;

public class CotacaoUnavailableException extends RuntimeException {
    public CotacaoUnavailableException(String date) {
        super("Cotação do dólar indisponível para a data " + date + ". Tente novamente mais tarde.");
    }
}
