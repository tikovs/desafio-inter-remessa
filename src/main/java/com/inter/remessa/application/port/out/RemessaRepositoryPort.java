package com.inter.remessa.application.port.out;

import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.Remessa;

import java.time.LocalDate;

public interface RemessaRepositoryPort {
    Remessa save(Remessa remessa);
    Money totalRemessasHoje(Long pessoaId, LocalDate date);
}
