package com.inter.remessa.adapter.in.web.pessoa;

import com.inter.remessa.domain.model.TipoPessoa;

public record PessoaResponse(Long id, String nome, String email, TipoPessoa tipoPessoa) {}
