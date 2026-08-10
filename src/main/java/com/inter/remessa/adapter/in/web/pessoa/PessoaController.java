package com.inter.remessa.adapter.in.web.pessoa;

import com.inter.remessa.application.usecase.CriarPessoaFisicaCommand;
import com.inter.remessa.application.usecase.CriarPessoaJuridicaCommand;
import com.inter.remessa.application.usecase.CriarPessoaService;
import com.inter.remessa.domain.model.PessoaFisica;
import com.inter.remessa.domain.model.PessoaJuridica;
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
@RequestMapping("/pessoas")
@Tag(name = "Pessoas", description = "Cadastro de Pessoa Física (CPF) e Pessoa Jurídica (CNPJ)")
class PessoaController {

    private final CriarPessoaService criarPessoaService;

    PessoaController(CriarPessoaService criarPessoaService) {
        this.criarPessoaService = criarPessoaService;
    }

    @PostMapping("/fisica")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar Pessoa Física",
            description = """
                    Cadastra uma Pessoa Física com carteira zerada. \
                    A senha é armazenada como hash bcrypt. \
                    CPF deve ter 11 dígitos numéricos sem máscara.\
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pessoa Física criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "CPF ou e-mail em formato inválido"),
            @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado")
    })
    PessoaResponse criarFisica(@RequestBody PessoaFisicaRequest request) {
        PessoaFisica pessoa = criarPessoaService.criar(
                new CriarPessoaFisicaCommand(request.nome(), request.email(), request.senha(), request.cpf()));
        return new PessoaResponse(pessoa.getId(), pessoa.getName(), pessoa.getEmail(), pessoa.getType());
    }

    @PostMapping("/juridica")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar Pessoa Jurídica",
            description = """
                    Cadastra uma Pessoa Jurídica com carteira zerada. \
                    A senha é armazenada como hash bcrypt. \
                    CNPJ suporta o formato alfanumérico vigente desde 31/07/2026 (IN RFB 2.229/2024): \
                    12 posições alfanuméricas + 2 dígitos verificadores numéricos, sem máscara.\
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pessoa Jurídica criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "CNPJ ou e-mail em formato inválido"),
            @ApiResponse(responseCode = "409", description = "E-mail ou CNPJ já cadastrado")
    })
    PessoaResponse criarJuridica(@RequestBody PessoaJuridicaRequest request) {
        PessoaJuridica pessoa = criarPessoaService.criar(
                new CriarPessoaJuridicaCommand(request.nome(), request.email(), request.senha(), request.cnpj()));
        return new PessoaResponse(pessoa.getId(), pessoa.getRazaoSocial(), pessoa.getEmail(), pessoa.getType());
    }
}
