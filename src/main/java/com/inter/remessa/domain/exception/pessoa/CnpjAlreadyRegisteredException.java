package com.inter.remessa.domain.exception.pessoa;

public class CnpjAlreadyRegisteredException extends RuntimeException {
    public CnpjAlreadyRegisteredException(String cnpj) {
        super("CNPJ já cadastrado: " + cnpj);
    }
}
