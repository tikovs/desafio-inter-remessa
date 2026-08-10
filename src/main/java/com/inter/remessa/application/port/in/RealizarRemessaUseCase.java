package com.inter.remessa.application.port.in;

import com.inter.remessa.application.usecase.remessa.RealizarRemessaCommand;
import com.inter.remessa.domain.model.Remessa;

public interface RealizarRemessaUseCase {
    Remessa realizar(RealizarRemessaCommand command);
}
