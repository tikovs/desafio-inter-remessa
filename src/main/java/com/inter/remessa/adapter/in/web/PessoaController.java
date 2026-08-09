package com.inter.remessa.adapter.in.web;

import com.inter.remessa.application.usecase.CriarPessoaFisicaCommand;
import com.inter.remessa.application.usecase.CriarPessoaJuridicaCommand;
import com.inter.remessa.application.usecase.CriarPessoaService;
import com.inter.remessa.domain.model.PessoaFisica;
import com.inter.remessa.domain.model.PessoaJuridica;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pessoas")
class PessoaController {

    private final CriarPessoaService criarPessoaService;

    PessoaController(CriarPessoaService criarPessoaService) {
        this.criarPessoaService = criarPessoaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PessoaResponse criar(@RequestBody PessoaRequest request) {
        if (request.cpf() != null) {
            PessoaFisica pessoa = criarPessoaService.criar(
                    new CriarPessoaFisicaCommand(request.nome(), request.email(), request.senha(), request.cpf()));
            return new PessoaResponse(pessoa.getId(), pessoa.getName(), pessoa.getEmail(), pessoa.getType());
        } else {
            PessoaJuridica pessoa = criarPessoaService.criar(
                    new CriarPessoaJuridicaCommand(request.nome(), request.email(), request.senha(), request.cnpj()));
            return new PessoaResponse(pessoa.getId(), pessoa.getRazaoSocial(), pessoa.getEmail(), pessoa.getType());
        }
    }
}
