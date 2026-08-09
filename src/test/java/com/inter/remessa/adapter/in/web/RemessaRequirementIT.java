package com.inter.remessa.adapter.in.web;

import com.inter.remessa.application.port.out.CotacaoProviderPort;
import com.inter.remessa.domain.model.Money;
import com.inter.remessa.domain.model.Pessoa;
import com.inter.remessa.domain.model.PessoaFisica;
import com.inter.remessa.domain.model.PessoaJuridica;
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
class RemessaRequirementIT {

    @TestConfiguration
    static class StubCotacao {
        @Bean
        @Primary
        CotacaoProviderPort cotacaoProvider() {
            return mock(CotacaoProviderPort.class);
        }
    }

    @Autowired WebApplicationContext context;
    @Autowired EntityManager em;
    @Autowired CotacaoProviderPort cotacaoProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        when(cotacaoProvider.getCotacaoDolar(any(LocalDate.class))).thenReturn(new BigDecimal("5.00"));
    }

    // --- helpers ---

    private PessoaFisica pf(String email, String cpf, BigDecimal saldo) {
        PessoaFisica p = new PessoaFisica("Test", email, "$hash", cpf);
        em.persist(p);
        em.persist(new Wallet(p, Money.ofReais(saldo)));
        em.flush();
        return p;
    }

    private PessoaJuridica pj(String email, String cnpj, BigDecimal saldo) {
        PessoaJuridica p = new PessoaJuridica("Empresa", email, "$hash", cnpj);
        em.persist(p);
        em.persist(new Wallet(p, Money.ofReais(saldo)));
        em.flush();
        return p;
    }

    private Wallet wallet(Pessoa pessoa) {
        em.flush();
        em.clear();
        return em.createQuery("SELECT w FROM Wallet w WHERE w.pessoa = :p", Wallet.class)
                .setParameter("p", em.find(pessoa.getClass(), pessoa.getId()))
                .getSingleResult();
    }

    private String json(Long remetenteId, Long destinatarioId, String valor) {
        return """
                {"remetenteId":%d,"destinatarioId":%d,"valor":%s}
                """.formatted(remetenteId, destinatarioId, valor);
    }

    // --- testes ---

    @Test
    @DisplayName("Should convert BRL to USD using cotacaoCompra field from BCB API")
    void shouldConvertBrlToUsdUsingCotacaoCompra() throws Exception {
        PessoaFisica remetente = pf("conv-rem@test.com", "40111111111", new BigDecimal("500.00"));
        PessoaFisica destinatario = pf("conv-dest@test.com", "40211111111", BigDecimal.ZERO);

        // rate = 5.00 → R$500 / 5.00 = US$100
        mockMvc.perform(post("/remessas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(remetente.getId(), destinatario.getId(), "500.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorReais").value(500.00))
                .andExpect(jsonPath("$.valorDolares").value(100.00))
                .andExpect(jsonPath("$.cotacaoUtilizada").value(5.00))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    @DisplayName("Should deduct BRL from sender wallet and add USD to recipient wallet")
    void shouldDeductBrlFromSenderAndAddUsdToRecipient() throws Exception {
        PessoaFisica remetente = pf("deduct-rem@test.com", "41111111111", new BigDecimal("1000.00"));
        PessoaFisica destinatario = pf("deduct-dest@test.com", "41211111111", BigDecimal.ZERO);

        mockMvc.perform(post("/remessas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(remetente.getId(), destinatario.getId(), "500.00")))
                .andExpect(status().isCreated());

        Wallet walletRem = wallet(remetente);
        Wallet walletDest = wallet(destinatario);

        assertThat(walletRem.getBalanceBrl()).isEqualTo(Money.ofReais(new BigDecimal("500.00")));
        assertThat(walletDest.getBalanceUsd()).isEqualTo(Money.ofReais(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("Should return 422 when sender has insufficient BRL balance")
    void shouldReturn422WhenSenderHasInsufficientBalance() throws Exception {
        PessoaFisica remetente = pf("insuf-rem@test.com", "42111111111", new BigDecimal("100.00"));
        PessoaFisica destinatario = pf("insuf-dest@test.com", "42211111111", BigDecimal.ZERO);

        mockMvc.perform(post("/remessas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(remetente.getId(), destinatario.getId(), "500.00")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Should return 422 when PessoaFisica exceeds daily limit of R$10,000")
    void shouldReturn422WhenPessoaFisicaExceedsDailyLimitOf10000() throws Exception {
        PessoaFisica remetente = pf("pf-limit-rem@test.com", "43111111111", new BigDecimal("15000.00"));
        PessoaFisica destinatario = pf("pf-limit-dest@test.com", "43211111111", BigDecimal.ZERO);

        // primeira remessa de R$10.000 — deve passar
        mockMvc.perform(post("/remessas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(remetente.getId(), destinatario.getId(), "10000.00")))
                .andExpect(status().isCreated());

        em.flush();

        // segunda remessa de R$1 — deve falhar por limite diário
        mockMvc.perform(post("/remessas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(remetente.getId(), destinatario.getId(), "1.00")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Should return 422 when PessoaJuridica exceeds daily limit of R$50,000")
    void shouldReturn422WhenPessoaJuridicaExceedsDailyLimitOf50000() throws Exception {
        PessoaJuridica remetente = pj("pj-limit-rem@test.com", "44111111000181", new BigDecimal("55000.00"));
        PessoaFisica destinatario = pf("pj-limit-dest@test.com", "44211111111", BigDecimal.ZERO);

        // primeira remessa de R$50.000 — deve passar
        mockMvc.perform(post("/remessas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(remetente.getId(), destinatario.getId(), "50000.00")))
                .andExpect(status().isCreated());

        em.flush();

        // segunda remessa de R$1 — deve falhar por limite diário
        mockMvc.perform(post("/remessas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(remetente.getId(), destinatario.getId(), "1.00")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Should allow remessa from PessoaFisica to PessoaJuridica without restriction")
    void shouldAllowRemessaFromPessoaFisicaToPessoaJuridica() throws Exception {
        PessoaFisica remetente = pf("pf-pj-rem@test.com", "45111111111", new BigDecimal("1000.00"));
        PessoaJuridica destinatario = pj("pf-pj-dest@test.com", "45111111000181", BigDecimal.ZERO);

        mockMvc.perform(post("/remessas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(remetente.getId(), destinatario.getId(), "500.00")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should allow remessa from PessoaJuridica to PessoaFisica without restriction")
    void shouldAllowRemessaFromPessoaJuridicaToPessoaFisica() throws Exception {
        PessoaJuridica remetente = pj("pj-pf-rem@test.com", "46111111000181", new BigDecimal("1000.00"));
        PessoaFisica destinatario = pf("pj-pf-dest@test.com", "46111111111", BigDecimal.ZERO);

        mockMvc.perform(post("/remessas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(remetente.getId(), destinatario.getId(), "500.00")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return 503 and leave wallet balance unchanged when cotação is unavailable")
    void shouldReturn503AndLeaveBalanceUnchangedWhenCotacaoUnavailable() throws Exception {
        PessoaFisica remetente = pf("rollback-rem@test.com", "47111111111", new BigDecimal("1000.00"));
        PessoaFisica destinatario = pf("rollback-dest@test.com", "47211111111", BigDecimal.ZERO);

        when(cotacaoProvider.getCotacaoDolar(any(LocalDate.class)))
                .thenThrow(new com.inter.remessa.domain.exception.CotacaoIndisponiveException("test"));

        mockMvc.perform(post("/remessas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(remetente.getId(), destinatario.getId(), "500.00")))
                .andExpect(status().isServiceUnavailable());

        Wallet walletRem = wallet(remetente);
        assertThat(walletRem.getBalanceBrl()).isEqualTo(Money.ofReais(new BigDecimal("1000.00")));
    }
}
