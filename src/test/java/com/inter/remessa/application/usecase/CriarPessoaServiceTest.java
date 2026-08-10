package com.inter.remessa.application.usecase;

import com.inter.remessa.application.port.out.PessoaRepositoryPort;
import com.inter.remessa.application.port.out.WalletRepositoryPort;
import com.inter.remessa.application.usecase.pessoa.CriarPessoaFisicaCommand;
import com.inter.remessa.application.usecase.pessoa.CriarPessoaService;
import com.inter.remessa.domain.exception.pessoa.EmailAlreadyRegisteredException;
import com.inter.remessa.domain.model.PessoaFisica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CriarPessoaServiceTest {

    private PessoaRepositoryPort repository;
    private WalletRepositoryPort walletRepository;
    private CriarPessoaService service;

    @BeforeEach
    void setUp() {
        repository = mock(PessoaRepositoryPort.class);
        walletRepository = mock(WalletRepositoryPort.class);
        service = new CriarPessoaService(repository, walletRepository, new BCryptPasswordEncoder());
    }

    @Test
    @DisplayName("Should save PessoaFisica when all data is valid")
    void shouldSavePessoaFisicaWhenAllDataIsValid() {
        when(repository.existsByEmail(any())).thenReturn(false);
        when(repository.existsByCpf(any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PessoaFisica result = service.criar(new CriarPessoaFisicaCommand("João Silva", "joao@email.com", "senha123", "12345678909"));

        verify(repository).save(any(PessoaFisica.class));
        assertThat(result.getName()).isEqualTo("João Silva");
        assertThat(result.getEmail()).isEqualTo("joao@email.com");
        assertThat(result.getCpf()).isEqualTo("12345678909");
    }

    @Test
    @DisplayName("Should throw EmailAlreadyRegisteredException when email already exists")
    void shouldThrowEmailAlreadyRegisteredExceptionWhenEmailAlreadyExists() {
        when(repository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThatThrownBy(() ->
                service.criar(new CriarPessoaFisicaCommand("João", "joao@email.com", "senha123", "12345678909"))
        ).isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should store password as bcrypt hash, not plain text")
    void shouldStorePasswordAsBcryptHashWhenCreatingPessoaFisica() {
        when(repository.existsByEmail(any())).thenReturn(false);
        when(repository.existsByCpf(any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PessoaFisica result = service.criar(new CriarPessoaFisicaCommand("João", "joao@email.com", "senha123", "12345678909"));

        assertThat(result.getPasswordHash()).startsWith("$2a$");
        assertThat(result.getPasswordHash()).isNotEqualTo("senha123");
    }
}
