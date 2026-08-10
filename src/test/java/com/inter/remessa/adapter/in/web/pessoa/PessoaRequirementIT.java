package com.inter.remessa.adapter.in.web.pessoa;

import com.inter.remessa.domain.model.TipoPessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PessoaRequirementIT {

    @LocalServerPort
    int port;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {})
                .build();
    }

    @Test
    @DisplayName("Should create PessoaFisica with name, email, password and CPF returning 201")
    void shouldCreatePessoaFisicaWithRequiredFields() {
        String body = """
                {"nome":"João Silva","email":"joao-pf-req@test.com","senha":"senha123","cpf":"10111111111"}
                """;

        ResponseEntity<PessoaResponse> response = client.post()
                .uri("/pessoas/fisica")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(PessoaResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().nome()).isEqualTo("João Silva");
        assertThat(response.getBody().email()).isEqualTo("joao-pf-req@test.com");
        assertThat(response.getBody().tipoPessoa()).isEqualTo(TipoPessoa.FISICA);
    }

    @Test
    @DisplayName("Should create PessoaJuridica with name, email, password and CNPJ returning 201")
    void shouldCreatePessoaJuridicaWithRequiredFields() {
        String body = """
                {"nome":"Empresa LTDA","email":"empresa-pj-req@test.com","senha":"senha123","cnpj":"98765432000101"}
                """;

        ResponseEntity<PessoaResponse> response = client.post()
                .uri("/pessoas/juridica")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(PessoaResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().tipoPessoa()).isEqualTo(TipoPessoa.JURIDICA);
    }

    @Test
    @DisplayName("Should return 409 when email is already registered by another user")
    void shouldReturn409WhenEmailAlreadyRegistered() {
        String first = """
                {"nome":"Ana Lima","email":"dup-email-req@test.com","senha":"s","cpf":"20111111111"}
                """;
        String second = """
                {"nome":"Bruno Lima","email":"dup-email-req@test.com","senha":"s","cpf":"20211111111"}
                """;

        client.post().uri("/pessoas/fisica").contentType(MediaType.APPLICATION_JSON).body(first).retrieve().toBodilessEntity();

        ResponseEntity<Void> response = client.post()
                .uri("/pessoas/fisica")
                .contentType(MediaType.APPLICATION_JSON)
                .body(second)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should return 409 when CPF is already registered by another PessoaFisica")
    void shouldReturn409WhenCpfAlreadyRegistered() {
        String first = """
                {"nome":"Carlos A","email":"dup-cpf-a@test.com","senha":"s","cpf":"30111111111"}
                """;
        String second = """
                {"nome":"Carlos B","email":"dup-cpf-b@test.com","senha":"s","cpf":"30111111111"}
                """;

        client.post().uri("/pessoas/fisica").contentType(MediaType.APPLICATION_JSON).body(first).retrieve().toBodilessEntity();

        ResponseEntity<Void> response = client.post()
                .uri("/pessoas/fisica")
                .contentType(MediaType.APPLICATION_JSON)
                .body(second)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should return 409 when CNPJ is already registered by another PessoaJuridica")
    void shouldReturn409WhenCnpjAlreadyRegistered() {
        String first = """
                {"nome":"Empresa A","email":"dup-cnpj-a@test.com","senha":"s","cnpj":"11222333000181"}
                """;
        String second = """
                {"nome":"Empresa B","email":"dup-cnpj-b@test.com","senha":"s","cnpj":"11222333000181"}
                """;

        client.post().uri("/pessoas/juridica").contentType(MediaType.APPLICATION_JSON).body(first).retrieve().toBodilessEntity();

        ResponseEntity<Void> response = client.post()
                .uri("/pessoas/juridica")
                .contentType(MediaType.APPLICATION_JSON)
                .body(second)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
