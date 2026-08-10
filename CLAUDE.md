# Desafio Técnico Java — Banco Inter (Serviço de Remessa)

Contexto do projeto para qualquer sessão do Claude Code. Reflete decisões de design já fechadas. Se surgir um problema ou alternativa melhor, **sempre apontar e sugerir**, mesmo que contrarie o que está aqui.

## Idioma

Este documento fica em português. Código (classes, métodos, variáveis, testes) fica em inglês, **exceto os termos de domínio brasileiro** que não têm equivalente semântico em inglês:

| Termo | Razão |
|-------|-------|
| `Remessa` | Transferência financeira internacional — "remittance" não captura a semântica de câmbio embutida |
| `Pessoa`, `PessoaFisica`, `PessoaJuridica` | Conceitos legais brasileiros (PF/PJ); nenhum equivalente direto em inglês |
| `Cotacao` | Taxa PTAX específica do BCB; "exchange rate" é genérico demais |
| `Saldo` | Usado em exceções de domínio (`SaldoInsufficientException`) |
| `Limite` | Limite diário regulatório (`LimiteExceededException`) |
| `Carteira` | Mantida como `Wallet` por ser conceito genérico de infraestrutura financeira |

CPF, CNPJ e Razão Social aparecem como campos/getters em português nos DTOs e nas entidades de domínio porque são siglas/termos regulatórios brasileiros sem tradução.

## Objetivo

API REST de remessa financeira entre Pessoa Física (PF) e Pessoa Jurídica (PJ). Uma remessa = conversão de moeda (BRL → USD) + transferência do valor convertido, tudo em uma transação atômica.

## Stack

- **Java 21** (LTS)
- **Spring Boot 4.1.0** — starters modularizados: `spring-boot-starter-webmvc` (não `-web`), `spring-boot-h2console`
- **Maven**
- **H2** em memória (`jdbc:h2:mem:remessadb;DB_CLOSE_DELAY=-1`)
- **Spring Data JPA** / Hibernate — `ddl-auto=create-drop`
- **Redis** — cache de cotação via `@Cacheable` no `CotacaoService`
- **SpringDoc OpenAPI 3** — Swagger UI em `/swagger-ui.html`

## Arquitetura: Hexagonal (Ports & Adapters)

```
com.inter.remessa
 ├── config              → OpenApiConfig, RedisConfig, BcbClientConfig, SecurityConfig, DevDataSeeder
 ├── domain
 │    ├── model         → Pessoa, PessoaFisica, PessoaJuridica, Wallet, Remessa, Money, Cotacao, TipoPessoa
 │    └── exception
 │         ├── cotacao  → CotacaoUnavailableException
 │         ├── pessoa   → EmailAlreadyRegisteredException, CpfAlreadyRegisteredException,
 │         │               CnpjAlreadyRegisteredException, InvalidCpfException,
 │         │               InvalidCnpjException, InvalidEmailException
 │         └── remessa  → SaldoInsufficientException, LimiteExceededException,
 │                         WalletNotFoundException
 ├── application
 │    ├── port
 │    │    ├── in        → RealizarRemessaUseCase
 │    │    └── out       → WalletRepositoryPort, CotacaoProviderPort, PessoaRepositoryPort,
 │    │                     RemessaRepositoryPort, CotacaoRepositoryPort
 │    ├── usecase
 │    │    ├── cotacao   → CotacaoService (@Cacheable)
 │    │    ├── pessoa    → CriarPessoaService, CriarPessoaFisicaCommand, CriarPessoaJuridicaCommand
 │    │    └── remessa   → RealizarRemessaService, RealizarRemessaCommand
 │    └── validator      → RemessaValidator (interface), RemessaValidationContext,
 │                         SaldoSufficientValidator, LimiteDailyValidator
 └── adapter
      ├── in/web
      │    ├── pessoa    → PessoaController (POST /pessoas/fisica, POST /pessoas/juridica)
      │    │                PessoaFisicaRequest, PessoaJuridicaRequest, PessoaResponse
      │    ├── remessa   → RemessaController (POST /remessas)
      │    │                RemessaRequest, RemessaResponse
      │    └── GlobalExceptionHandler
      └── out
           ├── persistence
           │    ├── cotacao  → CotacaoJpaAdapter, CotacaoJpaRepository
           │    ├── pessoa   → PessoaJpaAdapter, PessoaJpaRepository,
           │    │               PessoaFisicaJpaRepository, PessoaJuridicaJpaRepository
           │    ├── remessa  → RemessaJpaAdapter, RemessaJpaRepository
           │    ├── wallet   → WalletJpaAdapter, WalletJpaRepository
           │    └── MoneyConverter  (AttributeConverter compartilhado)
           └── bcb         → CotacaoBcbAdapter, CotacaoBcbResponse (PTAX API OData)
```

**Decisões de nomenclatura:**
- `port.in` / `port.out` — vocabulário canônico de Ports & Adapters (Cockburn / buckpal).
- `config` na raiz — infraestrutura transversal do Spring, não pertence a nenhuma camada de domínio.
- `validator` em `application`, não em `domain` — são `@Component` do Spring que orquestram regras sobre o `Remessa` já montado.

**Trade-off consciente:** anotações JPA diretamente nas classes de `domain.model`. Hexagonal puro exigiria mappers separados; o ganho de isolamento não justifica a complexidade extra para o escopo do desafio.

**Cache:** implementado via `@Cacheable` em `CotacaoService` (application layer), não como um adapter separado. O Spring abstrai o Redis via `CacheManager` — a camada de aplicação não sabe se está falando com Redis ou memória.

## Money (Value Object)

- Armazena valor internamente como **`long` em centavos** — aritmética inteira, zero erro de arredondamento.
- **`BigDecimal` entra apenas na conversão de moeda** (divisão pela cotação), com `RoundingMode.HALF_EVEN` (arredondamento bancário).
- **Factories por convenção** (`ofReais`, `ofCents`) — `record` público exige que o construtor canônico tenha acesso pelo menos igual ao do record; `new Money(x)` é tecnicamente alcançável. A convenção compensa o ganho de `equals`/`hashCode`/imutabilidade grátis do `record`.

## Convenção: quando usar `record` (Java 21)

**Usar `record`:**
- DTOs da camada web (`RemessaRequest`, `RemessaResponse`, `PessoaFisicaRequest`, `PessoaJuridicaRequest`)
- Commands dos use cases (`RealizarRemessaCommand`, `CriarPessoaFisicaCommand`…)
- Mapeamento de resposta de API externa (`CotacaoBcbResponse`) — Jackson deserializa record nativamente
- `Money` — `equals`/`hashCode`/`toString` de graça, imutabilidade estrutural

**NÃO usar `record`:**
- `Pessoa`, `PessoaFisica`, `PessoaJuridica`, `Wallet`, `Remessa` — são `@Entity` JPA (Hibernate precisa de construtor sem argumentos, suporte a proxy/lazy loading)
- Exceptions — `record` não pode estender `RuntimeException`

## Pessoa / PessoaFisica / PessoaJuridica

- Herança JPA com `@Inheritance(strategy = InheritanceType.JOINED)` — tabela `pessoa` (nome, email, senha) + `pessoa_fisica` (cpf) + `pessoa_juridica` (cnpj).
- Tipo (PF/PJ) resolvido por **método abstrato** `getType()` na hierarquia — não por enum salvo separadamente.
- **Senha**: hash bcrypt via `spring-security-crypto`. O hash acontece em `CriarPessoaService` (application layer), não na entidade — mantém `domain` livre de dependência de framework.
- **Validação de formato nos construtores** com `java.util.regex.Pattern` puro (sem Bean Validation) — garante entidades sempre válidas independente do ponto de entrada. DTOs da camada web têm `@Pattern` como segunda camada de defesa para rejeição HTTP 400 antecipada.
- **Email** normalizado para minúsculo no construtor — `User@test.com` e `user@test.com` seriam o mesmo endereço, mas a constraint `UNIQUE` do banco trataria como diferentes.
- **CPF**: `^\d{11}$` — 11 dígitos numéricos, sem máscara.
- **CNPJ**: `^[A-Z0-9]{12}\d{2}$` — formato alfanumérico vigente desde 31/07/2026 (IN RFB 2.229/2024). Regex `^\d{14}$` rejeitaria empresas recém-abertas.
- Unicidade: `@Column(unique = true)` em `email`, `cpf` e `cnpj`; `CriarPessoaService` verifica antes do `save` e lança exceção de domínio clara (`EmailAlreadyRegisteredException`, `CpfAlreadyRegisteredException`, `CnpjAlreadyRegisteredException`).
- **`@Transactional`** em ambos os métodos `criar()` de `CriarPessoaService` — garante atomicidade entre o `save` da Pessoa e o `save` da Wallet (sem ele, falha no segundo `save` deixaria a Pessoa sem Wallet).
- `PessoaJuridica` expõe `getRazaoSocial()` como alias semântico de `getNome()` — sem duplicar campo, sem mexer no mapeamento JPA.

## Pontos a discutir com avaliadores

- **`nome` vs `razão social`:** O enunciado usa "nome completo" para ambos os tipos. `PessoaJuridica` expõe `getRazaoSocial()` como alias semântico — mantém o vocabulário do domínio financeiro sem duplicar campo. Trade-off documentado: campo único na base satisfaz o requisito, getter semântico sinaliza consciência do domínio.
- **CNPJ alfanumérico:** Mudança regulatória de 31/07/2026 (IN RFB 2.229/2024). Regex `^\d{14}$` rejeitaria CNPJs válidos. Validação de dígito verificador (módulo 11 sobre ASCII-48) não implementada — o enunciado pede unicidade, não validação de DV.
- **Dois endpoints de Pessoa:** `POST /pessoas/fisica` e `POST /pessoas/juridica` em vez de um endpoint unificado com campo nulo — contratos distintos, Swagger mais claro, sem lógica `if (cpf != null)`.
- **JPA no domínio:** anotações JPA diretamente nas entidades de domínio (sem mapper) — trade-off explícito de pragmatismo vs pureza hexagonal.

## Cotação de câmbio (API do BCB)

- Endpoint: `https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia(dataCotacao=@dataCotacao)?@dataCotacao='MM-DD-YYYY'&$format=json`
- Campo usado: `cotacaoCompra` (item `value[0]` — padrão OData).
- **Formato de data americano** (`MM-DD-AAAA`) — armadilha frequente.
- Fechamento PTAX publicado ~13h (horário de Brasília). Antes disso, o endpoint retorna array vazio.
- **Fallback genérico:** `CotacaoService` tenta a data solicitada e recua dia a dia (até 10 dias) se o BCB retornar vazio — sem `if (isWeekend())` hardcoded, cobre fim de semana, feriado e consulta antes da PTAX.
- **Timezone:** `LocalDate.now(ZoneId.of("America/Sao_Paulo"))` em todo código que precisa de "hoje". Container Docker roda em UTC; sem fuso explícito, o limite diário é contado na janela errada e a cotação é buscada para a data UTC incorreta.
- Base URL externalizada em `application.properties` (`bcb.ptax.base-url`).

## Cache (Redis)

- `CotacaoProviderPort` é a interface; `CotacaoService` implementa via `@Cacheable(value = "cotacoes", key = "#date")`.
- Cache por data é seguro: cotação de fechamento PTAX não muda após publicação.
- **Cotacao.data tem `@Column(unique = true)`** — evita duplicatas caso o cache seja contornado e `CotacaoService.save()` seja chamado duas vezes para o mesmo dia. O `CotacaoService` também verifica `findByData()` antes de salvar (idempotência em nível de aplicação).
- `CotacaoRepositoryPort` expõe `findByData(LocalDate)` (não `findLatest()`) — consulta precisa pela data solicitada, sem ambiguidade sobre qual cotação é "a mais recente".
- TTL padrão 24h, configurável via `cache.cotacoes.ttl-hours`.
- **`BigDecimal` é `final`** — `GenericJacksonJsonRedisSerializer` usa `DefaultTyping.NON_FINAL` e não inclui `@class` para classes finais; Jackson deserializa o número como `Double` causando `ClassCastException`. Fix: `ObjectMapper` do Redis configurado com `USE_BIG_DECIMAL_FOR_FLOATS`.
- Sem Redis, Spring usa `ConcurrentMapCacheManager` (fallback em memória, `@ConditionalOnMissingBean`).

## Fluxo da remessa (`RealizarRemessaService`)

Ordem por custo, não por regra arbitrária:

1. Buscar carteira do remetente (dado interno)
2. Rodar todos os `RemessaValidator` (saldo suficiente + limite diário — independentes, sem `@Order`)
3. Buscar cotação (cache → BCB → fallback)
4. Converter: **dividir** por `cotacaoCompra` (`cotacaoCompra` = quantos reais por 1 dólar)
5. Débito/crédito nas carteiras + salvar remessa — tudo dentro de `@Transactional`

Validators como Strategy: cada regra é um `@Component implements RemessaValidator`. Nova regra = nova classe, sem tocar nas existentes (Open/Closed).

## Limites diários

- PF: R$ 10.000/dia
- PJ: R$ 50.000/dia
- Acumulado consultado via `RemessaRepository.totalRemessasHoje()` — dado interno, sem chamada externa.
- `Remessa.dataHora` usa `ZoneId.of("America/Sao_Paulo")` para ficar na mesma janela temporal que o `LocalDate` passado para a consulta.

## Convenção de desenvolvimento: TDD

**Toda funcionalidade nova começa pelo teste.** Ordem: teste falhando → código mínimo → refatorar → próximo comportamento.

## Testes

62 testes: unitários (`*Test.java`) + integração (`*IT.java`). Spring Boot 4.1 removeu os test slices clássicos (`@DataJpaTest`, `@WebMvcTest`, `@MockBean`). Padrão adotado: `@SpringBootTest(webEnvironment = MOCK ou RANDOM_PORT)` + `@Transactional` + `@TestConfiguration @Primary` para substituir beans por mocks.

### Nomenclatura

- Idioma: **inglês** — nome do método, `@DisplayName`, variáveis locais.
- Padrão: `should<ExpectedBehavior>When<Condition>`.

```java
@Test
@DisplayName("Should convert R$ 543.00 to US$ 100.00 when exchange rate is 5.43")
void shouldConvertReaisToDollarsWhenExchangeRateIsValid() { ... }
```
