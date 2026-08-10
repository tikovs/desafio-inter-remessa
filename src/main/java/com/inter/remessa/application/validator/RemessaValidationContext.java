package com.inter.remessa.application.validator;

import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.TipoPessoa;

public record RemessaValidationContext(
        Money valor,
        Money saldoRemetente,
        Money totalRemessasHoje,
        TipoPessoa tipoPessoa
) {}
