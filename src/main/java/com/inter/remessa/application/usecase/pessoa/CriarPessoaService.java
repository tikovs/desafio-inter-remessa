package com.inter.remessa.application.usecase.pessoa;

import com.inter.remessa.application.port.out.PessoaRepositoryPort;
import com.inter.remessa.application.port.out.WalletRepositoryPort;
import com.inter.remessa.domain.exception.pessoa.CnpjAlreadyRegisteredException;
import com.inter.remessa.domain.exception.pessoa.CpfAlreadyRegisteredException;
import com.inter.remessa.domain.exception.pessoa.EmailAlreadyRegisteredException;
import com.inter.remessa.domain.model.PessoaFisica;
import com.inter.remessa.domain.model.PessoaJuridica;
import com.inter.remessa.domain.model.Wallet;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public PessoaFisica criar(CriarPessoaFisicaCommand command) {
        if (repository.existsByEmail(command.email())) throw new EmailAlreadyRegisteredException(command.email());
        if (repository.existsByCpf(command.cpf())) throw new CpfAlreadyRegisteredException(command.cpf());
        String passwordHash = passwordEncoder.encode(command.password());
        PessoaFisica pessoa = new PessoaFisica(command.name(), command.email(), passwordHash, command.cpf());
        PessoaFisica saved = (PessoaFisica) repository.save(pessoa);
        walletRepository.save(new Wallet(saved));
        return saved;
    }

    @Transactional
    public PessoaJuridica criar(CriarPessoaJuridicaCommand command) {
        if (repository.existsByEmail(command.email())) throw new EmailAlreadyRegisteredException(command.email());
        if (repository.existsByCnpj(command.cnpj())) throw new CnpjAlreadyRegisteredException(command.cnpj());
        String passwordHash = passwordEncoder.encode(command.password());
        PessoaJuridica pessoa = new PessoaJuridica(command.razaoSocial(), command.email(), passwordHash, command.cnpj());
        PessoaJuridica saved = (PessoaJuridica) repository.save(pessoa);
        walletRepository.save(new Wallet(saved));
        return saved;
    }
}
