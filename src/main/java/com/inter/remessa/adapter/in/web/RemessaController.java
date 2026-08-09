package com.inter.remessa.adapter.in.web;

import com.inter.remessa.application.port.in.RealizarRemessaUseCase;
import com.inter.remessa.application.usecase.RealizarRemessaCommand;
import com.inter.remessa.domain.model.Remessa;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/remessas")
class RemessaController {

    private final RealizarRemessaUseCase realizarRemessaUseCase;

    RemessaController(RealizarRemessaUseCase realizarRemessaUseCase) {
        this.realizarRemessaUseCase = realizarRemessaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RemessaResponse realizar(@RequestBody RemessaRequest request) {
        Remessa remessa = realizarRemessaUseCase.realizar(
                new RealizarRemessaCommand(request.remetenteId(), request.destinatarioId(), request.valor()));
        return new RemessaResponse(
                remessa.getId(),
                remessa.getValorReais().toBigDecimal(),
                remessa.getValorDolares().toBigDecimal(),
                remessa.getCotacao(),
                "CONCLUIDA"
        );
    }
}
