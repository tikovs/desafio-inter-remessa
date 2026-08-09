package com.inter.remessa.application.validator;

import com.inter.remessa.domain.exception.SaldoInsuficienteException;
import org.springframework.stereotype.Component;

@Component
public class SaldoSuficienteValidator implements RemessaValidator {

    @Override
    public void validate(ValidacaoRemessaContext context) {
        if (context.walletRemetente().getBalanceBrl().isLessThan(context.valor())) {
            throw new SaldoInsuficienteException();
        }
    }
}
