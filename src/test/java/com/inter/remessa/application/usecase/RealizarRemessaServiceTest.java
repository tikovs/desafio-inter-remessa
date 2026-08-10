package com.inter.remessa.application.usecase;

import com.inter.remessa.application.port.out.CotacaoProviderPort;
import com.inter.remessa.application.port.out.RemessaRepositoryPort;
import com.inter.remessa.application.port.out.WalletRepositoryPort;
import com.inter.remessa.application.usecase.remessa.RealizarRemessaCommand;
import com.inter.remessa.application.usecase.remessa.RealizarRemessaService;
import com.inter.remessa.application.validator.LimiteDailyValidator;
import com.inter.remessa.application.validator.SaldoSufficientValidator;
import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.PessoaFisica;
import com.inter.remessa.domain.model.Remessa;
import com.inter.remessa.domain.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RealizarRemessaServiceTest {

    private WalletRepositoryPort walletRepository;
    private RemessaRepositoryPort remessaRepository;
    private CotacaoProviderPort cotacaoProvider;
    private RealizarRemessaService service;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepositoryPort.class);
        remessaRepository = mock(RemessaRepositoryPort.class);
        cotacaoProvider = mock(CotacaoProviderPort.class);
        service = new RealizarRemessaService(
                walletRepository,
                remessaRepository,
                cotacaoProvider,
                List.of(new SaldoSufficientValidator(), new LimiteDailyValidator())
        );
    }

    @Test
    @DisplayName("Should debit reais from sender and credit converted dollars to recipient")
    void shouldDebitReaisAndCreditDollarsWhenRemessaIsValid() {
        PessoaFisica remetente = new PessoaFisica("João Silva", "joao@test.com", "$hash", "12345678901");
        PessoaFisica destinatario = new PessoaFisica("Maria Souza", "maria@test.com", "$hash", "98765432100");

        // Remetente: R$ 1000 disponível; destinatário: sem saldo inicial
        Wallet walletRemetente = new Wallet(remetente, Money.ofReais(new BigDecimal("1000.00")));
        Wallet walletDestinatario = new Wallet(destinatario);

        when(walletRepository.findByPessoaId(1L)).thenReturn(walletRemetente);
        when(walletRepository.findByPessoaId(2L)).thenReturn(walletDestinatario);
        when(remessaRepository.totalRemessasHoje(eq(1L), any(LocalDate.class))).thenReturn(Money.ofCents(0));
        when(cotacaoProvider.getCotacaoDolar(any(LocalDate.class))).thenReturn(new BigDecimal("5.00"));

        service.realizar(new RealizarRemessaCommand(1L, 2L, new BigDecimal("500.00")));

        // R$ 1000 - R$ 500 = R$ 500
        assertThat(walletRemetente.getBalanceBrl()).isEqualTo(Money.ofReais(new BigDecimal("500.00")));
        // R$ 500 / 5,00 = US$ 100
        assertThat(walletDestinatario.getBalanceUsd()).isEqualTo(Money.ofReais(new BigDecimal("100.00")));
        verify(walletRepository).save(walletRemetente);
        verify(walletRepository).save(walletDestinatario);
        verify(remessaRepository).save(any(Remessa.class));
    }
}
