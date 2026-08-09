package com.inter.remessa.application.usecase;

import com.inter.remessa.application.port.out.PessoaRepositoryPort;
import com.inter.remessa.domain.exception.CnpjJaCadastradoException;
import com.inter.remessa.domain.exception.CpfJaCadastradoException;
import com.inter.remessa.domain.exception.EmailJaCadastradoException;
import com.inter.remessa.domain.model.PessoaFisica;
import com.inter.remessa.domain.model.PessoaJuridica;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CriarPessoaService {

    private final PessoaRepositoryPort repository;
    private final PasswordEncoder passwordEncoder;

    public CriarPessoaService(PessoaRepositoryPort repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public PessoaFisica criar(CriarPessoaFisicaCommand command) {
        if (repository.existsByEmail(command.email())) throw new EmailJaCadastradoException(command.email());
        if (repository.existsByCpf(command.cpf())) throw new CpfJaCadastradoException(command.cpf());
        String passwordHash = passwordEncoder.encode(command.password());
        PessoaFisica pessoa = new PessoaFisica(command.name(), command.email(), passwordHash, command.cpf());
        return (PessoaFisica) repository.save(pessoa);
    }

    public PessoaJuridica criar(CriarPessoaJuridicaCommand command) {
        if (repository.existsByEmail(command.email())) throw new EmailJaCadastradoException(command.email());
        if (repository.existsByCnpj(command.cnpj())) throw new CnpjJaCadastradoException(command.cnpj());
        String passwordHash = passwordEncoder.encode(command.password());
        PessoaJuridica pessoa = new PessoaJuridica(command.razaoSocial(), command.email(), passwordHash, command.cnpj());
        return (PessoaJuridica) repository.save(pessoa);
    }
}
