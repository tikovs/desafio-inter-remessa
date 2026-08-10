package com.inter.remessa.application.validator;

import com.inter.remessa.domain.exception.remessa.LimiteExceededException;
import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.TipoPessoa;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LimiteDailyValidator implements RemessaValidator {

    private static final Money LIMITE_PF = Money.ofReais(new BigDecimal("10000.00"));
    private static final Money LIMITE_PJ = Money.ofReais(new BigDecimal("50000.00"));

    @Override
    public void validate(RemessaValidationContext context) {
        Money limite = context.tipoPessoa() == TipoPessoa.FISICA ? LIMITE_PF : LIMITE_PJ;
        Money totalComEstaRemessa = context.totalRemessasHoje().add(context.valor());
        if (limite.isLessThan(totalComEstaRemessa)) {
            throw new LimiteExceededException();
        }
    }
}
