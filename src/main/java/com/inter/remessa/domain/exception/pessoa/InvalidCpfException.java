package com.inter.remessa.domain.exception.pessoa;

public class InvalidCpfException extends RuntimeException {
    public InvalidCpfException(String cpf) {
        super("Invalid CPF: " + cpf);
    }
}
