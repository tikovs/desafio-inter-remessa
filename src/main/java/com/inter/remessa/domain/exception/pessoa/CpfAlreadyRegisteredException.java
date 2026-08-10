package com.inter.remessa.domain.exception.pessoa;

public class CpfAlreadyRegisteredException extends RuntimeException {
    public CpfAlreadyRegisteredException(String cpf) {
        super("CPF já cadastrado: " + cpf);
    }
}
