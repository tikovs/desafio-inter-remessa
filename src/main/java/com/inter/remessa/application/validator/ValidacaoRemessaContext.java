package com.inter.remessa.application.validator;

import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.TipoPessoa;
import com.inter.remessa.domain.model.Wallet;

public record ValidacaoRemessaContext(
        Money valor,
        Wallet walletRemetente,
        Money totalRemessasHoje,
        TipoPessoa tipoPessoa
) {}
