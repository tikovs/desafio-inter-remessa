package com.inter.remessa.domain.exception.pessoa;

public class InvalidCnpjException extends RuntimeException {
    public InvalidCnpjException(String cnpj) {
        super("Invalid CNPJ: " + cnpj);
    }
}
