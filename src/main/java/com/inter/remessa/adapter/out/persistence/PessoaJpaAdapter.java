package com.inter.remessa.adapter.out.persistence;

import com.inter.remessa.application.port.out.PessoaRepositoryPort;
import com.inter.remessa.domain.model.Pessoa;
import org.springframework.stereotype.Repository;

@Repository
class PessoaJpaAdapter implements PessoaRepositoryPort {

    private final PessoaJpaRepository pessoaRepo;
    private final PessoaFisicaJpaRepository pessoaFisicaRepo;
    private final PessoaJuridicaJpaRepository pessoaJuridicaRepo;

    PessoaJpaAdapter(PessoaJpaRepository pessoaRepo,
                     PessoaFisicaJpaRepository pessoaFisicaRepo,
                     PessoaJuridicaJpaRepository pessoaJuridicaRepo) {
        this.pessoaRepo = pessoaRepo;
        this.pessoaFisicaRepo = pessoaFisicaRepo;
        this.pessoaJuridicaRepo = pessoaJuridicaRepo;
    }

    @Override
    public boolean existsByEmail(String email) {
        return pessoaRepo.existsByEmail(email);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return pessoaFisicaRepo.existsByCpf(cpf);
    }

    @Override
    public boolean existsByCnpj(String cnpj) {
        return pessoaJuridicaRepo.existsByCnpj(cnpj);
    }

    @Override
    public Pessoa save(Pessoa pessoa) {
        return pessoaRepo.save(pessoa);
    }
}
