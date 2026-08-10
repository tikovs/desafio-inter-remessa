package com.inter.remessa.application.usecase;

import com.inter.remessa.application.port.in.RealizarRemessaUseCase;
import com.inter.remessa.application.port.out.CotacaoProviderPort;
import com.inter.remessa.application.port.out.RemessaRepositoryPort;
import com.inter.remessa.application.port.out.WalletRepositoryPort;
import com.inter.remessa.application.validator.RemessaValidator;
import com.inter.remessa.application.validator.ValidacaoRemessaContext;
import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.Remessa;
import com.inter.remessa.domain.model.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class RealizarRemessaService implements RealizarRemessaUseCase {

    private final WalletRepositoryPort walletRepository;
    private final RemessaRepositoryPort remessaRepository;
    private final CotacaoProviderPort cotacaoProvider;
    private final List<RemessaValidator> validators;

    public RealizarRemessaService(
            WalletRepositoryPort walletRepository,
            RemessaRepositoryPort remessaRepository,
            CotacaoProviderPort cotacaoProvider,
            List<RemessaValidator> validators) {
        this.walletRepository = walletRepository;
        this.remessaRepository = remessaRepository;
        this.cotacaoProvider = cotacaoProvider;
        this.validators = validators;
    }

    @Override
    @Transactional
    public Remessa realizar(RealizarRemessaCommand command) {
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        Money valorReais = Money.ofReais(command.valorReais());

        // 1. Busca carteira do remetente
        Wallet walletRemetente = walletRepository.findByPessoaId(command.remetenteId());

        // 2. Roda validators (saldo suficiente, limite diário)
        Money totalHoje = remessaRepository.totalRemessasHoje(command.remetenteId(), hoje);
        ValidacaoRemessaContext ctx = new ValidacaoRemessaContext(
                valorReais,
                walletRemetente,
                totalHoje,
                walletRemetente.getPessoa().getType()

        );
        validators.forEach(v -> v.validate(ctx));

        // 3. Busca cotação
        BigDecimal cotacao = cotacaoProvider.getCotacaoDolar(hoje);

        // 4. Converte valor para dólares
        Money valorDolares = valorReais.convert(cotacao);

        // 5. Debita reais da carteira do remetente
        walletRemetente.debitReais(valorReais);

        // 6. Credita dólares na carteira do destinatário
        Wallet walletDestinatario = walletRepository.findByPessoaId(command.destinatarioId());
        walletDestinatario.creditDollars(valorDolares);

        // 7. Salva as duas carteiras
        walletRepository.save(walletRemetente);
        walletRepository.save(walletDestinatario);

        // 8. Registra a remessa para contar no limite diário futuro
        return remessaRepository.save(new Remessa(
                walletRemetente.getPessoa(),
                walletDestinatario.getPessoa(),
                valorReais,
                valorDolares,
                cotacao
        ));
    }
}
