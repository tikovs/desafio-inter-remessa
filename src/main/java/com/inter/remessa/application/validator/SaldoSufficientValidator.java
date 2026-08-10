package com.inter.remessa.application.validator;

import com.inter.remessa.domain.exception.remessa.SaldoInsufficientException;
import org.springframework.stereotype.Component;

@Component
public class SaldoSufficientValidator implements RemessaValidator {

    @Override
    public void validate(RemessaValidationContext context) {
        if (context.walletRemetente().getBalanceBrl().isLessThan(context.valor())) {
            throw new SaldoInsufficientException();
        }
    }
}
