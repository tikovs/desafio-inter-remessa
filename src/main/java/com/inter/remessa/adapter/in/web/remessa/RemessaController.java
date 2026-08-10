package com.inter.remessa.adapter.in.web.remessa;

import com.inter.remessa.application.port.in.RealizarRemessaUseCase;
import com.inter.remessa.application.usecase.remessa.RealizarRemessaCommand;
import com.inter.remessa.domain.model.Remessa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/remessas")
@Tag(name = "Remessas", description = "Transferências financeiras com conversão BRL → USD via PTAX/BCB")
class RemessaController {

    private final RealizarRemessaUseCase realizarRemessaUseCase;

    RemessaController(RealizarRemessaUseCase realizarRemessaUseCase) {
        this.realizarRemessaUseCase = realizarRemessaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Realizar remessa",
            description = """
                    Converte o valor em BRL para USD usando a cotação de compra (PTAX) do Banco Central \
                    e transfere os dólares para a carteira do destinatário. \
                    A operação é atômica: débito e crédito ocorrem na mesma transação.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Remessa realizada com sucesso"),
            @ApiResponse(responseCode = "422", description = "Saldo insuficiente ou limite diário excedido (PF: R$10.000, PJ: R$50.000)"),
            @ApiResponse(responseCode = "503", description = "Cotação do BCB indisponível para a data solicitada")
    })
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
