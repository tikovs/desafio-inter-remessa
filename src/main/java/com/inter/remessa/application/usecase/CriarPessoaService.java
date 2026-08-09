package com.inter.remessa.application.usecase;

import com.inter.remessa.application.port.out.PessoaRepositoryPort;
import com.inter.remessa.application.port.out.WalletRepositoryPort;
import com.inter.remessa.domain.exception.CnpjJaCadastradoException;
import com.inter.remessa.domain.exception.CpfJaCadastradoException;
import com.inter.remessa.domain.exception.EmailJaCadastradoException;
import com.inter.remessa.domain.model.Pessoa;
import com.inter.remessa.domain.model.PessoaFisica;
import com.inter.remessa.domain.model.PessoaJuridica;
import com.inter.remessa.domain.model.Wallet;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CriarPessoaService {

    private final PessoaRepositoryPort repository;
    private final WalletRepositoryPort walletRepository;
    private final PasswordEncoder passwordEncoder;

    public CriarPessoaService(PessoaRepositoryPort repository, WalletRepositoryPort walletRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PessoaFisica criar(CriarPessoaFisicaCommand command) {
        if (repository.existsByEmail(command.email())) throw new EmailJaCadastradoException(command.email());
        if (repository.existsByCpf(command.cpf())) throw new CpfJaCadastradoException(command.cpf());
        String passwordHash = passwordEncoder.encode(command.password());
        PessoaFisica pessoa = new PessoaFisica(command.name(), command.email(), passwordHash, command.cpf());
        PessoaFisica saved = (PessoaFisica) repository.save(pessoa);
        walletRepository.save(new Wallet(saved));
        return saved;
    }

    public PessoaJuridica criar(CriarPessoaJuridicaCommand command) {
        if (repository.existsByEmail(command.email())) throw new EmailJaCadastradoException(command.email());
        if (repository.existsByCnpj(command.cnpj())) throw new CnpjJaCadastradoException(command.cnpj());
        String passwordHash = passwordEncoder.encode(command.password());
        PessoaJuridica pessoa = new PessoaJuridica(command.razaoSocial(), command.email(), passwordHash, command.cnpj());
        PessoaJuridica saved = (PessoaJuridica) repository.save(pessoa);
        walletRepository.save(new Wallet(saved));
        return saved;
    }
}
