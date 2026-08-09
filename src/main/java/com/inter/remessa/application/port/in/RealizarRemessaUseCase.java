package com.inter.remessa.application.port.in;

import com.inter.remessa.application.usecase.RealizarRemessaCommand;

public interface RealizarRemessaUseCase {
    void realizar(RealizarRemessaCommand command);
}
