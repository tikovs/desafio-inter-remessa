# Desafio Técnico — Serviço de Remessa (Banco Inter)

API REST de remessa financeira com conversão BRL → USD em tempo real via PTAX (Banco Central do Brasil).

---

## Requisitos

**Sem Docker:**
- Java 21
- Maven (ou use o wrapper `./mvnw` incluído)

**Com Docker:**
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) — inclui Docker + Docker Compose. Java e Maven não precisam estar instalados.

---

## Como executar

### Opção 1 — Docker Compose (recomendado)

```bash
docker compose up --build
```

A aplicação sobe na porta `8080` com Redis na `6379`. Sem Redis, o cache cai automaticamente para memória.

```bash
docker compose up --build -d   # em segundo plano
docker compose down            # parar tudo
```

### Opção 2 — Local (sem Docker)

```bash
# Compilar
./mvnw clean package -DskipTests

# Executar
./mvnw spring-boot:run
```

Sem Redis rodando, a aplicação usa cache em memória automaticamente (fallback).

---

## Documentação da API

Com a aplicação no ar, acesse o Swagger UI:

**[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

O contrato completo (campos, tipos, códigos de resposta) está lá — não está duplicado neste README.

Schema OpenAPI em JSON: `http://localhost:8080/v3/api-docs`

---

## Dados de teste

Ao subir, o `DevDataSeeder` cria 5 usuários e imprime no log os `curl`s prontos para cada requisito:

| id | Tipo | E-mail | Saldo inicial |
|----|------|--------|---------------|
| 1 | PF | joao@example.com | R$ 15.000 |
| 2 | PJ | empresa@example.com | R$ 55.000 |
| 3 | PF | maria@example.com | R$ 0 (destino) |
| 4 | PF | carlos@example.com | R$ 0 (sem saldo) |
| 5 | PJ | empresadest@example.com | R$ 0 (destino) |

> **Console H2:** `http://localhost:8080/h2-console`
> JDBC URL: `jdbc:h2:mem:remessadb` · usuário: `sa` · senha: *(vazia)*

---

## Testando com curl — passo a passo

Os curls abaixo são **idênticos** para Docker e para execução local. A única diferença é como a aplicação é iniciada:

```bash
# Docker (recomendado)
docker compose up --build -d

# Local
./mvnw spring-boot:run
```

Aguarde a mensagem `Started RemessaApplication` no log antes de prosseguir. Os **5 usuários de teste** são criados automaticamente pelo `DevDataSeeder` (ids 1–5).

---

### 1. Cadastro de pessoas

**Criar Pessoa Física**
```bash
curl -s -X POST http://localhost:8080/pessoas/fisica \
  -H "Content-Type: application/json" \
  -d '{"nome":"Ana Souza","email":"ana@example.com","senha":"senha123","cpf":"33344455566"}' \
  | python3 -m json.tool
# → 201: { "id": 6, "nome": "Ana Souza", "tipoPessoa": "FISICA" }
```

**Criar Pessoa Jurídica (CNPJ alfanumérico — IN RFB 2.229/2024)**
```bash
curl -s -X POST http://localhost:8080/pessoas/juridica \
  -H "Content-Type: application/json" \
  -d '{"nome":"Nova Tech SA","email":"novatech@example.com","senha":"senha123","cnpj":"ABCD1234567801"}' \
  | python3 -m json.tool
# → 201: { "id": 7, "nome": "Nova Tech SA", "tipoPessoa": "JURIDICA" }
```

**E-mail duplicado → 409**
```bash
curl -s -X POST http://localhost:8080/pessoas/fisica \
  -H "Content-Type: application/json" \
  -d '{"nome":"Outro","email":"ana@example.com","senha":"senha123","cpf":"99988877766"}' \
  | python3 -m json.tool
# → 409: { "detail": "Email já cadastrado: ana@example.com" }
```

**CPF inválido (formato errado) → 400**
```bash
curl -s -X POST http://localhost:8080/pessoas/fisica \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste","email":"teste@example.com","senha":"senha123","cpf":"123"}' \
  | python3 -m json.tool
# → 400: { "detail": "Invalid CPF: 123" }
```

**CNPJ inválido → 400**
```bash
curl -s -X POST http://localhost:8080/pessoas/juridica \
  -H "Content-Type: application/json" \
  -d '{"nome":"Empresa","email":"emp@example.com","senha":"senha123","cnpj":"invalido"}' \
  | python3 -m json.tool
# → 400: { "detail": "Invalid CNPJ: invalido" }
```

---

### 2. Remessa com conversão BRL → USD

**Remessa válida — PF envia R$ 500 (usa cotação PTAX do BCB em tempo real)**
```bash
curl -s -X POST http://localhost:8080/remessas \
  -H "Content-Type: application/json" \
  -d '{"remetenteId":1,"destinatarioId":3,"valor":500}' \
  | python3 -m json.tool
# → 201: { "valorReais": 500.0, "valorDolares": 98.23, "cotacaoUtilizada": 5.0902, "status": "CONCLUIDA" }
```

---

### 3. Saldo insuficiente → 422

**Carlos (id=4) tem R$ 0 — qualquer remessa deve ser rejeitada**
```bash
curl -s -X POST http://localhost:8080/remessas \
  -H "Content-Type: application/json" \
  -d '{"remetenteId":4,"destinatarioId":3,"valor":1}' \
  | python3 -m json.tool
# → 422: { "detail": "Saldo insuficiente para realizar a remessa" }
```

---

### 4. Limite diário PF — R$ 10.000/dia

**Passo 1 — envia R$ 10.000 (deve passar — João tem R$ 15.000)**
```bash
curl -s -X POST http://localhost:8080/remessas \
  -H "Content-Type: application/json" \
  -d '{"remetenteId":1,"destinatarioId":3,"valor":10000}' \
  | python3 -m json.tool
# → 201: remessa concluída
```

**Passo 2 — tenta enviar R$ 1 a mais (limite esgotado) → 422**
```bash
curl -s -X POST http://localhost:8080/remessas \
  -H "Content-Type: application/json" \
  -d '{"remetenteId":1,"destinatarioId":3,"valor":1}' \
  | python3 -m json.tool
# → 422: { "detail": "Limite diário de remessa excedido" }
```

---

### 5. Limite diário PJ — R$ 50.000/dia

**Passo 1 — envia R$ 50.000 (deve passar — Empresa tem R$ 55.000)**
```bash
curl -s -X POST http://localhost:8080/remessas \
  -H "Content-Type: application/json" \
  -d '{"remetenteId":2,"destinatarioId":3,"valor":50000}' \
  | python3 -m json.tool
# → 201: remessa concluída
```

**Passo 2 — tenta enviar R$ 1 a mais → 422**
```bash
curl -s -X POST http://localhost:8080/remessas \
  -H "Content-Type: application/json" \
  -d '{"remetenteId":2,"destinatarioId":3,"valor":1}' \
  | python3 -m json.tool
# → 422: { "detail": "Limite diário de remessa excedido" }
```

---

### 6. Remessa entre tipos — PF → PJ e PJ → PF (ambos permitidos)

> **Nota:** execute estes antes dos testes de limite acima, ou reinicie a aplicação para resetar o H2.

**PF → PJ**
```bash
curl -s -X POST http://localhost:8080/remessas \
  -H "Content-Type: application/json" \
  -d '{"remetenteId":1,"destinatarioId":5,"valor":100}' \
  | python3 -m json.tool
# → 201: remessa concluída
```

**PJ → PF**
```bash
curl -s -X POST http://localhost:8080/remessas \
  -H "Content-Type: application/json" \
  -d '{"remetenteId":2,"destinatarioId":3,"valor":100}' \
  | python3 -m json.tool
# → 201: remessa concluída
```

---

### Reiniciar os dados de teste

O H2 é em memória — reiniciar a aplicação limpa tudo e recria os 5 usuários do `DevDataSeeder`:

```bash
# Docker
docker compose restart app

# Local
# Ctrl+C e ./mvnw spring-boot:run novamente
```

---

## Como executar os testes

```bash
./mvnw test
```

**62 testes** no total:

| Classe | Tipo | O que cobre |
|--------|------|-------------|
| `MoneyTest` | Unitário | Conversão, aritmética em centavos, arredondamento bancário |
| `WalletTest` | Unitário | Débito e crédito em BRL e USD |
| `PessoaFisicaTest` | Unitário | Validação de CPF e e-mail no construtor |
| `PessoaJuridicaTest` | Unitário | Validação de CNPJ alfanumérico (IN RFB 2.229/2024) |
| `SaldoSufficientValidatorTest` | Unitário | Rejeita remessa quando saldo insuficiente |
| `LimiteDailyValidatorTest` | Unitário | Limite PF R$10k/dia · PJ R$50k/dia |
| `RealizarRemessaServiceTest` | Unitário | Orquestração: carteira → validação → cotação → débito/crédito |
| `CriarPessoaServiceTest` | Unitário | Criação de PF/PJ com hash bcrypt e carteira automática |
| `CotacaoServiceTest` | Unitário | Fallback genérico de cotação (walk-back por dia) |
| `CotacaoBcbAdapterTest` | Unitário | Chamada HTTP ao BCB · formato de data americano · resposta vazia |
| `RemessaRequirementIT` | Integração | Todos os requisitos de remessa contra H2 real |
| `PessoaRequirementIT` | Integração | Criação PF/PJ · unicidade e-mail, CPF, CNPJ |
| `CotacaoWeekendIT` | Integração | Walk-back para sexta em fim de semana e dia útil sem PTAX |
| `CotacaoCacheIT` | Integração | Cache: BCB chamado 1× por data, hit na segunda chamada |
| `PessoaEmailUnicidadeIT` | Integração | Constraint JPA de unicidade de e-mail no H2 |

---

## Estrutura de pastas

```
src/main/java/com/inter/remessa/
 ├── config/                         → OpenApiConfig, RedisConfig, BcbClientConfig, SecurityConfig, DevDataSeeder
 ├── domain/
 │    ├── model/                     → Pessoa, PessoaFisica, PessoaJuridica, Wallet, Remessa, Money, Cotacao, TipoPessoa
 │    └── exception/
 │         ├── cotacao/              → CotacaoUnavailableException
 │         ├── pessoa/               → EmailAlreadyRegisteredException, CpfAlreadyRegisteredException,
 │         │                            CnpjAlreadyRegisteredException, InvalidCpfException,
 │         │                            InvalidCnpjException, InvalidEmailException
 │         └── remessa/              → SaldoInsufficientException, LimiteExceededException, WalletNotFoundException
 ├── application/
 │    ├── port/
 │    │    ├── in/                   → RealizarRemessaUseCase
 │    │    └── out/                  → WalletRepositoryPort, CotacaoProviderPort, PessoaRepositoryPort,
 │    │                                 RemessaRepositoryPort, CotacaoRepositoryPort
 │    ├── usecase/
 │    │    ├── cotacao/              → CotacaoService
 │    │    ├── pessoa/               → CriarPessoaService, CriarPessoaFisicaCommand, CriarPessoaJuridicaCommand
 │    │    └── remessa/              → RealizarRemessaService, RealizarRemessaCommand
 │    └── validator/                 → RemessaValidator, RemessaValidationContext,
 │                                      SaldoSufficientValidator, LimiteDailyValidator
 └── adapter/
      ├── in/web/
      │    ├── pessoa/               → PessoaController, PessoaFisicaRequest, PessoaJuridicaRequest, PessoaResponse
      │    ├── remessa/              → RemessaController, RemessaRequest, RemessaResponse
      │    └── GlobalExceptionHandler
      └── out/
           ├── persistence/
           │    ├── cotacao/         → CotacaoJpaAdapter, CotacaoJpaRepository
           │    ├── pessoa/          → PessoaJpaAdapter, PessoaJpaRepository,
           │    │                       PessoaFisicaJpaRepository, PessoaJuridicaJpaRepository
           │    ├── remessa/         → RemessaJpaAdapter, RemessaJpaRepository
           │    ├── wallet/          → WalletJpaAdapter, WalletJpaRepository
           │    └── MoneyConverter
           └── bcb/                  → CotacaoBcbAdapter, CotacaoBcbResponse
```

---

## Decisões de design

### Arquitetura Hexagonal (Ports & Adapters)

O projeto segue o padrão Ports & Adapters com separação clara entre `domain`, `application` e `adapter`. As anotações JPA ficam diretamente nas entidades de domínio — concessão prática que evita mappers intermediários sem comprometer o isolamento para o escopo do desafio.

### Money como centavos (`long`)

Toda aritmética monetária usa `long` em centavos internamente. `BigDecimal` entra apenas na conversão de moeda (divisão pela cotação), com `RoundingMode.HALF_EVEN` (arredondamento bancário). Isso elimina erros de ponto flutuante e mantém comparações simples.

### CNPJ alfanumérico

O validador usa `^[A-Z0-9]{12}\d{2}$` em vez de `^\d{14}$`. A Receita Federal emite CNPJs alfanuméricos desde 31/07/2026 (IN RFB nº 2.229/2024) — uma validação só numérica rejeitaria empresas recém-abertas.

### Fallback de cotação (genérico)

A API do BCB não publica PTAX em fins de semana, feriados, ou antes das ~13h em dias úteis. O `CotacaoService` implementa um loop que tenta a data solicitada e recua dia a dia (até 10 dias) até encontrar uma cotação disponível — sem verificação hardcoded de `isWeekend()`.

### Timezone

Datas de remessa e `dataHora` de registro usam `ZoneId.of("America/Sao_Paulo")`. O container Docker roda em UTC; sem o fuso explícito, o limite diário seria contado em janelas erradas e a cotação buscada para a data UTC incorreta.

### Spring Boot 4.1

O Spring Boot 4.1 removeu os test slices clássicos (`@DataJpaTest`, `@WebMvcTest`, `@MockBean`). Os testes de integração usam `@SpringBootTest(webEnvironment = MOCK)` + `@Transactional` + `@TestConfiguration @Primary` para substituir beans por mocks.

### Cache de cotações (Redis)

A cotação de fechamento PTAX é publicada uma vez por dia e não muda. O cache usa a data como chave (`cotacoes::2026-08-09`), com TTL de 24h (configurável via `cache.cotacoes.ttl-hours`). Sem Redis, a aplicação usa cache em memória automaticamente.

`BigDecimal` é uma classe `final` — o `GenericJacksonJsonRedisSerializer` não inclui `@class` no JSON, causando deserialização como `Double`. O `ObjectMapper` do Redis é configurado com `USE_BIG_DECIMAL_FOR_FLOATS` para contornar isso.

### Stack

- Java 21 · Spring Boot 4.1 · Spring Data JPA · H2 (em memória) · Hibernate 7
- Cache: Redis 7 · fallback em memória sem Redis
- Documentação: SpringDoc OpenAPI 3 (`/swagger-ui.html`)
- Testes: JUnit 5 · AssertJ · Mockito
- Segurança: `spring-security-crypto` para hash bcrypt (sem Spring Security completo)
- Docker: multi-stage build (Maven + JRE Alpine) · Docker Compose com Redis
