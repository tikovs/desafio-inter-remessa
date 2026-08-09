package com.inter.remessa.adapter.in.web;

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
        PessoaRequest request = new PessoaRequest(
                "João Silva", "joao@test.com", "senha123", "12345678901", null);

        ResponseEntity<PessoaResponse> response = client.post()
                .uri("/pessoas")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(PessoaResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().nome()).isEqualTo("João Silva");
        assertThat(response.getBody().email()).isEqualTo("joao@test.com");
        assertThat(response.getBody().tipoPessoa()).isEqualTo(TipoPessoa.FISICA);
    }
}
