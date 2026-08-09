package com.inter.remessa.domain.exception;

public class CnpjInvalidoException extends RuntimeException {
    public CnpjInvalidoException(String cnpj) {
        super("CNPJ inválido: " + cnpj);
    }
}
