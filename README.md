# Desafio Técnico — Serviço de Remessa (Banco Inter)

API REST de remessa financeira com conversão BRL → USD em tempo real via API do Banco Central do Brasil.

---

## Requisitos

**Sem Docker:**
- Java 21
- Maven (ou use o wrapper `./mvnw` incluído — não precisa instalar nada)

**Com Docker:**
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (inclui Docker + Docker Compose)

> Com Docker não é necessário ter Java ou Maven instalados — a compilação acontece dentro do container.

---

## Como executar

### Opção 1 — Docker Compose (recomendado)

Sobe a aplicação e o Redis juntos com um único comando:

```bash
docker compose up --build
```

A aplicação sobe na porta `8080` e o Redis na `6379`. O cache de cotações usa Redis automaticamente.

```bash
# Parar tudo
docker compose down

# Rodar em segundo plano
docker compose up --build -d
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

## Dados de teste

Ao subir, o `DevDataSeeder` cria 5 usuários e imprime no log os comandos `curl` prontos para testar cada requisito:

```
╔══════════════════════════════════════════════════════════════╗
║               DADOS DE TESTE — REQUISITOS                   ║
╠══════════════════════════════════════════════════════════════╣
║  PF  id=1  joao@example.com          saldo=R$15.000        ║
║  PJ  id=2  empresa@example.com        saldo=R$55.000        ║
║  PF  id=3  maria@example.com          saldo=R$0 (destino)  ║
║  PF  id=4  carlos@example.com         saldo=R$0 (sem $)    ║
║  PJ  id=5  empresadest@example.com    saldo=R$0 (destino)  ║
╚══════════════════════════════════════════════════════════════╝
```

> **Console H2:** `http://localhost:8080/h2-console`
> JDBC URL: `jdbc:h2:mem:remessadb` · usuário: `sa` · senha: *(vazia)*

---

## Como executar os testes

```bash
# Todos os testes (unitários + integração)
./mvnw test
```

São **61 testes** no total, organizados em duas categorias:

### Testes unitários (`*Test.java`)

| Classe | O que cobre |
|---|---|
| `MoneyTest` | Value object: conversão, aritmética em centavos, arredondamento bancário |
| `WalletTest` | Débito, crédito em BRL e USD |
| `PessoaFisicaTest` | Validação de CPF e e-mail no construtor |
| `PessoaJuridicaTest` | Validação de CNPJ alfanumérico (formato pós IN RFB 2.229/2024) |
| `SaldoSuficienteValidatorTest` | Rejeita remessa quando saldo é insuficiente |
| `LimiteDiarioValidatorTest` | Limite PF R$10k/dia · PJ R$50k/dia |
| `RealizarRemessaServiceTest` | Orquestração do fluxo: busca carteira → valida → cotação → débito/crédito |
| `CriarPessoaServiceTest` | Criação de PF e PJ com hash de senha e criação automática de carteira |
| `CotacaoServiceTest` | Lógica de fim de semana e fallback via BCB |
| `CotacaoBcbAdapterTest` | Chamada HTTP ao BCB e tratamento de resposta vazia |

### Testes de integração (`*IT.java`)

| Classe | O que cobre |
|---|---|
| `RemessaRequirementIT` | **Todos os requisitos de remessa** (ver tabela abaixo) |
| `PessoaRequirementIT` | Criação de PF/PJ, unicidade de e-mail, CPF e CNPJ |
| `CotacaoWeekendIT` | Retorna cotação do banco sem chamar BCB no fim de semana; fallback para sexta |
| `CotacaoCacheIT` | Cache Redis: BCB chamado 1x por data, hit na segunda chamada, isolamento por data |
| `PessoaEmailUnicidadeIT` | Unicidade de e-mail a nível de constraint JPA no H2 |

---

## Requisitos testados e validados

| # | Requisito | Cobertura |
|---|---|---|
| 1 | Conversão BRL → USD usando `cotacaoCompra` do BCB | `RemessaRequirementIT` + curl manual |
| 2 | Débito na carteira do remetente, crédito na do destinatário | `RemessaRequirementIT` |
| 3 | Saldo insuficiente → HTTP 422 | `RemessaRequirementIT` + curl manual |
| 4 | Limite diário PF R$10.000 → HTTP 422 ao exceder | `RemessaRequirementIT` + curl manual |
| 5 | Limite diário PJ R$50.000 → HTTP 422 ao exceder | `RemessaRequirementIT` + curl manual |
| 6 | Remessa PF → PJ permitida | `RemessaRequirementIT` + curl manual |
| 7 | Remessa PJ → PF permitida | `RemessaRequirementIT` + curl manual |
| 8 | Cotação indisponível → HTTP 503 + rollback (saldo intacto) | `RemessaRequirementIT` |
| 9 | Fim de semana: usa última cotação do banco sem chamar BCB | `CotacaoWeekendIT` |
| 10 | Criação de PF (CPF único) e PJ (CNPJ único) → HTTP 201 | `PessoaRequirementIT` |
| 11 | E-mail, CPF e CNPJ duplicados → HTTP 409 | `PessoaRequirementIT` |

---

## Testando com Docker

Após subir com `docker compose up --build`, a aplicação e o Redis estão prontos. Os IDs dos usuários de teste são impressos no log — use-os nos curls abaixo.

### 1. Verificar que a aplicação subiu

```bash
curl -s http://localhost:8080/pessoas
# → HTTP 405 (endpoint existe, método GET não permitido — app está no ar)
```

### 2. Criar uma remessa e confirmar conversão BRL→USD

```bash
curl -s -X POST http://localhost:8080/remessas \
  -H 'Content-Type: application/json' \
  -d '{"remetenteId":1,"destinatarioId":3,"valor":500}'
# → {"id":1,"valorReais":500.0,"valorDolares":98.23,"cotacaoUtilizada":5.0902,"status":"CONCLUIDA"}
```

### 3. Confirmar que a cotação foi cacheada no Redis

```bash
# Entrar no container do Redis
docker compose exec redis redis-cli

# Dentro do redis-cli:
KEYS cotacoes::*
# → "cotacoes::2024-08-09"

GET "cotacoes::2024-08-09"
# → "5.0902"

TTL "cotacoes::2024-08-09"
# → 86394  (segundos restantes até expirar)
```

### 4. Confirmar que o cache evita segunda chamada ao BCB

```bash
# Segunda remessa no mesmo dia — cotação vem do Redis, não do BCB
curl -s -X POST http://localhost:8080/remessas \
  -H 'Content-Type: application/json' \
  -d '{"remetenteId":1,"destinatarioId":3,"valor":100}'
# → {"status":"CONCLUIDA"} (sem nova chamada ao BCB no log)
```

### 5. Testar erros de negócio

```bash
# Saldo insuficiente → 422
curl -s -X POST http://localhost:8080/remessas \
  -H 'Content-Type: application/json' \
  -d '{"remetenteId":4,"destinatarioId":3,"valor":500}'
# → HTTP 422 {"detail":"Saldo insuficiente para realizar a remessa"}

# Limite diário PF R$10.000 excedido → 422
curl -s -X POST http://localhost:8080/remessas \
  -H 'Content-Type: application/json' \
  -d '{"remetenteId":1,"destinatarioId":3,"valor":10000}'
# primeira: 201 CONCLUIDA

curl -s -X POST http://localhost:8080/remessas \
  -H 'Content-Type: application/json' \
  -d '{"remetenteId":1,"destinatarioId":3,"valor":1}'
# segunda: 422 {"detail":"Limite diário de remessa excedido"}
```

### 6. Ver logs em tempo real

```bash
docker compose logs -f app
```

---

## Testado manualmente via curl

Todos os requisitos foram testados com a aplicação rodando localmente. Exemplo de saída real:

```bash
# REQ 1 — Conversão BRL→USD com cotação real do BCB
curl -s -X POST http://localhost:8080/remessas \
  -H 'Content-Type: application/json' \
  -d '{"remetenteId":1,"destinatarioId":3,"valor":500}'
# → {"id":1,"valorReais":500.0,"valorDolares":98.23,"cotacaoUtilizada":5.0902,"status":"CONCLUIDA"}

# REQ 2 — Saldo insuficiente
curl -s -X POST http://localhost:8080/remessas \
  -H 'Content-Type: application/json' \
  -d '{"remetenteId":4,"destinatarioId":3,"valor":500}'
# → HTTP 422 {"detail":"Saldo insuficiente para realizar a remessa"}

# REQ 3a — Limite diário PF R$10.000 (segundo envio após atingir o limite)
curl -s -X POST http://localhost:8080/remessas \
  -H 'Content-Type: application/json' \
  -d '{"remetenteId":1,"destinatarioId":3,"valor":1}'
# → HTTP 422 {"detail":"Limite diário de remessa excedido"}

# REQ 4a — PF → PJ permitido
curl -s -X POST http://localhost:8080/remessas \
  -H 'Content-Type: application/json' \
  -d '{"remetenteId":1,"destinatarioId":5,"valor":100}'
# → HTTP 201 {"status":"CONCLUIDA"}
```

Os IDs exatos são impressos no log da aplicação ao iniciar.

---

## Decisões de design

### Arquitetura Hexagonal (Ports & Adapters)

O projeto segue o padrão Ports & Adapters com separação clara entre `domain`, `application` e `adapter`. As anotações JPA ficam diretamente nas entidades de domínio — uma concessão prática que evita mappers intermediários sem comprometer o isolamento para o escopo do desafio.

### Money como centavos (`long`)

Toda aritmética monetária usa `long` em centavos internamente. `BigDecimal` entra apenas na conversão de moeda (divisão pela cotação), com `RoundingMode.HALF_EVEN` (arredondamento bancário). Isso elimina erros de ponto flutuante e mantém comparações simples.

### CNPJ alfanumérico

O validador de CNPJ usa a regex `^[A-Z0-9]{12}\d{2}$` em vez de `^\d{14}$`. A Receita Federal passou a emitir CNPJs alfanuméricos a partir de 31/07/2026 (IN RFB nº 2.229/2024) — uma validação só com dígitos rejeitaria empresas recém-abertas.

### Cotação no fim de semana

A API do BCB não publica cotação nos finais de semana. O `CotacaoService` persiste cada cotação obtida no banco e, quando a data solicitada cair em sábado ou domingo, retorna a última cotação armazenada sem chamar o BCB. Se o banco estiver vazio, busca a sexta-feira anterior via `TemporalAdjusters.previous(FRIDAY)`.

### Spring Boot 4.1

O Spring Boot 4.1 removeu os test slices clássicos (`@DataJpaTest`, `@WebMvcTest`, `@MockBean`). Os testes de integração usam `@SpringBootTest(webEnvironment = MOCK)` + `@Transactional` + `@TestConfiguration @Primary` para substituir beans por mocks, sem precisar de slices ou bibliotecas extras.

### Cache de cotações (Redis)

A cotação de fechamento PTAX é publicada uma única vez por dia (~13h) pelo BCB e não muda após publicação. Por isso o cache é seguro: a chave é a própria data (`cotacoes::2024-08-09`), então cada dia tem sua entrada independente — não há risco de servir cotação do dia anterior.

O TTL padrão é 24h (configurável via `cache.cotacoes.ttl-hours`) e serve apenas para limpeza de entradas antigas no Redis. Sem Redis, a aplicação usa cache em memória automaticamente.

Para inspecionar o cache com `redis-cli`:

```bash
redis-cli KEYS "cotacoes::*"          # lista as datas cacheadas
redis-cli GET "cotacoes::2024-08-09"  # valor da cotação
redis-cli TTL "cotacoes::2024-08-09"  # tempo restante em segundos
```

### Stack

- Java 21 · Spring Boot 4.1 · Spring Data JPA · H2 (em memória) · Hibernate 7
- Cache: Redis 7 via `spring-boot-starter-data-redis` · fallback em memória sem Redis
- Testes: JUnit 5 · AssertJ · Mockito (via `spring-boot-starter-test`)
- Segurança: `spring-security-crypto` para hash bcrypt de senhas (sem Spring Security completo)
- Docker: multi-stage build (Maven + JRE Alpine) · Docker Compose com Redis
