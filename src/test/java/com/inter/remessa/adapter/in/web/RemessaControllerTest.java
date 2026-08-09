package com.inter.remessa.adapter.in.web;

import com.inter.remessa.application.port.out.CotacaoProviderPort;
import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.PessoaFisica;
import com.inter.remessa.domain.model.Wallet;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
@ActiveProfiles("test")
class RemessaControllerTest {

    // @TestConfiguration como static inner class é automaticamente carregada pelo @SpringBootTest
    @TestConfiguration
    static class StubCotacao {
        @Bean
        @Primary
        CotacaoProviderPort cotacaoProvider() {
            return mock(CotacaoProviderPort.class);
        }
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EntityManager em;

    // Injeta o mock declarado em StubCotacao para configurar stubbings por teste
    @Autowired
    private CotacaoProviderPort cotacaoProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        when(cotacaoProvider.getCotacaoDolar(any(LocalDate.class)))
                .thenReturn(new BigDecimal("5.00"));
    }

    @Test
    @DisplayName("Should return 201 and transfer R$500 to US$100 when exchange rate is 5.00")
    void shouldReturn201AndTransferFundsWhenRemessaIsValid() throws Exception {
        // Cria pessoas e carteiras diretamente no repositório, sem passar pelo endpoint
        PessoaFisica remetente = new PessoaFisica("João Silva", "joao@remessa.com", "$hash", "12345678901");
        PessoaFisica destinatario = new PessoaFisica("Maria Souza", "maria@remessa.com", "$hash", "98765432100");
        em.persist(remetente);
        em.persist(destinatario);

        Wallet walletRemetente = new Wallet(remetente, Money.ofReais(new BigDecimal("1000.00")));
        Wallet walletDestinatario = new Wallet(destinatario);
        em.persist(walletRemetente);
        em.persist(walletDestinatario);
        em.flush();

        String requestJson = """
                {"remetenteId": %d, "destinatarioId": %d, "valor": 500.00}
                """.formatted(remetente.getId(), destinatario.getId());

        mockMvc.perform(post("/remessas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.valorReais").value(500.00))
                .andExpect(jsonPath("$.valorDolares").value(100.00))
                .andExpect(jsonPath("$.cotacaoUtilizada").value(5.00));

        // Confirma saldos finais lendo direto do banco, dentro da mesma transação
        em.flush();
        em.clear();
        Wallet updatedRemetente = em.find(Wallet.class, walletRemetente.getId());
        Wallet updatedDestinatario = em.find(Wallet.class, walletDestinatario.getId());
        assertThat(updatedRemetente.getBalanceBrl()).isEqualTo(Money.ofReais(new BigDecimal("500.00")));
        assertThat(updatedDestinatario.getBalanceUsd()).isEqualTo(Money.ofReais(new BigDecimal("100.00")));
    }
}
