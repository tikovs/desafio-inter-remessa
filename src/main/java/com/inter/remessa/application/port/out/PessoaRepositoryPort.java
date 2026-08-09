package com.inter.remessa.application.port.out;

import com.inter.remessa.domain.model.Pessoa;

public interface PessoaRepositoryPort {
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    boolean existsByCnpj(String cnpj);
    Pessoa save(Pessoa pessoa);
}
