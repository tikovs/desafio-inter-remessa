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
        // PF com R$15.000 — permite testar conversão E limite diário (R$10k)
        Pessoa pf = criarPessoaService.criar(new CriarPessoaFisicaCommand(
                "João Silva", "joao@example.com", "senha123", "12345678901"));
        credit(pf, "15000.00");

        // PJ com R$55.000 — permite testar limite diário PJ (R$50k)
        Pessoa pj = criarPessoaService.criar(new CriarPessoaJuridicaCommand(
                "Empresa Teste LTDA", "empresa@example.com", "senha123", "12345678000195"));
        credit(pj, "55000.00");

        // PF destinatário (saldo zero — só recebe)
        Pessoa dest = criarPessoaService.criar(new CriarPessoaFisicaCommand(
                "Maria Destino", "maria@example.com", "senha123", "98765432100"));

        // PF sem saldo — para testar erro de saldo insuficiente
        Pessoa semSaldo = criarPessoaService.criar(new CriarPessoaFisicaCommand(
                "Carlos Sem Saldo", "carlos@example.com", "senha123", "11122233344"));

        // PJ destinatária — para testar remessa PF→PJ e PJ→PF
        Pessoa pjDest = criarPessoaService.criar(new CriarPessoaJuridicaCommand(
                "Empresa Destino SA", "empresadest@example.com", "senha123", "99988877700011"));

        long pfId      = pf.getId();
        long pjId      = pj.getId();
        long destId    = dest.getId();
        long semSaldoId = semSaldo.getId();
        long pjDestId  = pjDest.getId();

        log.info("");
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║               DADOS DE TESTE — REQUISITOS                   ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║  PF  id={}  joao@example.com          saldo=R$15.000      ║", pfId);
        log.info("║  PJ  id={}  empresa@example.com        saldo=R$55.000      ║", pjId);
        log.info("║  PF  id={}  maria@example.com          saldo=R$0 (destino) ║", destId);
        log.info("║  PF  id={}  carlos@example.com         saldo=R$0 (sem $)   ║", semSaldoId);
        log.info("║  PJ  id={}  empresadest@example.com    saldo=R$0 (destino) ║", pjDestId);
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║  CURLS PARA TESTAR CADA REQUISITO:                          ║");
        log.info("║                                                              ║");
        log.info("║  [REQ 1] Conversão BRL→USD (cotacaoCompra do BCB)           ║");
        log.info("║  R$500 / cotação ≈ US$xx.xx                                 ║");
        log.info("║  curl -s -X POST http://localhost:8080/remessas \\           ║");
        log.info("║    -H 'Content-Type: application/json' \\                    ║");
        log.info("║    -d '{{\"remetenteId\":{},\"destinatarioId\":{},\"valor\":500}}'", pfId, destId);
        log.info("║                                                              ║");
        log.info("║  [REQ 2] Saldo insuficiente → 422                           ║");
        log.info("║  curl -s -X POST http://localhost:8080/remessas \\           ║");
        log.info("║    -H 'Content-Type: application/json' \\                    ║");
        log.info("║    -d '{{\"remetenteId\":{},\"destinatarioId\":{},\"valor\":500}}'", semSaldoId, destId);
        log.info("║                                                              ║");
        log.info("║  [REQ 3a] Limite diário PF R$10.000 → 422                  ║");
        log.info("║  (envie primeiro R$10.000, depois qualquer valor adicional) ║");
        log.info("║  curl -s -X POST http://localhost:8080/remessas \\           ║");
        log.info("║    -H 'Content-Type: application/json' \\                    ║");
        log.info("║    -d '{{\"remetenteId\":{},\"destinatarioId\":{},\"valor\":10000}}'", pfId, destId);
        log.info("║  # segundo envio (deve retornar 422):                        ║");
        log.info("║  curl -s -X POST http://localhost:8080/remessas \\           ║");
        log.info("║    -H 'Content-Type: application/json' \\                    ║");
        log.info("║    -d '{{\"remetenteId\":{},\"destinatarioId\":{},\"valor\":1}}'", pfId, destId);
        log.info("║                                                              ║");
        log.info("║  [REQ 3b] Limite diário PJ R$50.000 → 422                  ║");
        log.info("║  curl -s -X POST http://localhost:8080/remessas \\           ║");
        log.info("║    -H 'Content-Type: application/json' \\                    ║");
        log.info("║    -d '{{\"remetenteId\":{},\"destinatarioId\":{},\"valor\":50000}}'", pjId, destId);
        log.info("║  # segundo envio (deve retornar 422):                        ║");
        log.info("║  curl -s -X POST http://localhost:8080/remessas \\           ║");
        log.info("║    -H 'Content-Type: application/json' \\                    ║");
        log.info("║    -d '{{\"remetenteId\":{},\"destinatarioId\":{},\"valor\":1}}'", pjId, destId);
        log.info("║                                                              ║");
        log.info("║  [REQ 4a] PF → PJ permitido → 201                          ║");
        log.info("║  curl -s -X POST http://localhost:8080/remessas \\           ║");
        log.info("║    -H 'Content-Type: application/json' \\                    ║");
        log.info("║    -d '{{\"remetenteId\":{},\"destinatarioId\":{},\"valor\":100}}'", pfId, pjDestId);
        log.info("║                                                              ║");
        log.info("║  [REQ 4b] PJ → PF permitido → 201                          ║");
        log.info("║  curl -s -X POST http://localhost:8080/remessas \\           ║");
        log.info("║    -H 'Content-Type: application/json' \\                    ║");
        log.info("║    -d '{{\"remetenteId\":{},\"destinatarioId\":{},\"valor\":100}}'", pjId, destId);
        log.info("╚══════════════════════════════════════════════════════════════╝");
        log.info("");
    }

    private void credit(Pessoa pessoa, String valor) {
        Wallet wallet = walletRepository.findByPessoaId(pessoa.getId());
        wallet.creditReais(Money.ofReais(new BigDecimal(valor)));
        walletRepository.save(wallet);
    }
}
