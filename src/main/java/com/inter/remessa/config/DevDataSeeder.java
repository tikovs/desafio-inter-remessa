package com.inter.remessa.config;

import com.inter.remessa.application.port.out.WalletRepositoryPort;
import com.inter.remessa.application.usecase.CriarPessoaFisicaCommand;
import com.inter.remessa.application.usecase.CriarPessoaJuridicaCommand;
import com.inter.remessa.application.usecase.CriarPessoaService;
import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.Pessoa;
import com.inter.remessa.domain.model.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("!test")
class DevDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final CriarPessoaService criarPessoaService;
    private final WalletRepositoryPort walletRepository;

    DevDataSeeder(CriarPessoaService criarPessoaService, WalletRepositoryPort walletRepository) {
        this.criarPessoaService = criarPessoaService;
        this.walletRepository = walletRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Pessoa pf = criarPessoaService.criar(new CriarPessoaFisicaCommand(
                "João Silva", "joao@example.com", "senha123", "12345678901"));
        Wallet walletPf = walletRepository.findByPessoaId(pf.getId());
        walletPf.creditReais(Money.ofReais(new BigDecimal("5000.00")));
        walletRepository.save(walletPf);

        Pessoa pj = criarPessoaService.criar(new CriarPessoaJuridicaCommand(
                "Empresa Teste LTDA", "empresa@example.com", "senha123", "12345678000195"));
        Wallet walletPj = walletRepository.findByPessoaId(pj.getId());
        walletPj.creditReais(Money.ofReais(new BigDecimal("20000.00")));
        walletRepository.save(walletPj);

        log.info("=== DEV DATA ===");
        log.info("PF id={} email=joao@example.com saldo=R$5.000,00", pf.getId());
        log.info("PJ id={} email=empresa@example.com saldo=R$20.000,00", pj.getId());
        log.info("================");
    }
}
