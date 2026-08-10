package com.inter.remessa.adapter.in.web.pessoa;

import com.inter.remessa.domain.model.TipoPessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PessoaControllerTest {

    @LocalServerPort
    int port;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("Should return 201 Created with PessoaResponse when creating a valid PessoaFisica")
    void shouldReturn201WhenCreatingValidPessoaFisica() {
        PessoaFisicaRequest request = new PessoaFisicaRequest(
                "João Silva", "joao-ctrl@test.com", "senha123", "12345678901");

        ResponseEntity<PessoaResponse> response = client.post()
                .uri("/pessoas/fisica")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(PessoaResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().nome()).isEqualTo("João Silva");
        assertThat(response.getBody().email()).isEqualTo("joao-ctrl@test.com");
        assertThat(response.getBody().tipoPessoa()).isEqualTo(TipoPessoa.FISICA);
    }

    @Test
    @DisplayName("Should return 201 Created with PessoaResponse when creating a valid PessoaJuridica")
    void shouldReturn201WhenCreatingValidPessoaJuridica() {
        PessoaJuridicaRequest request = new PessoaJuridicaRequest(
                "Empresa LTDA", "empresa-ctrl@test.com", "senha123", "12345678000195");

        ResponseEntity<PessoaResponse> response = client.post()
                .uri("/pessoas/juridica")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(PessoaResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().nome()).isEqualTo("Empresa LTDA");
        assertThat(response.getBody().email()).isEqualTo("empresa-ctrl@test.com");
        assertThat(response.getBody().tipoPessoa()).isEqualTo(TipoPessoa.JURIDICA);
    }
}
