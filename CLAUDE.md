# Desafio Técnico Java — Banco Inter (Serviço de Remessa)

Contexto do projeto para qualquer sessão do Claude Code. Este arquivo reflete decisões de design já fechadas em conversa anterior — use como ponto de partida, não como regra fixa. Se durante a implementação surgir um problema, risco ou alternativa melhor que essas decisões, **sempre apontar e sugerir a melhoria**, mesmo que contrarie o que está documentado aqui. Documentação desatualizada ou uma decisão que não se sustenta na prática vale mais ser corrigida do que seguida cegamente.

## Objetivo

API REST de remessa financeira entre usuários Pessoa Física (PF) e Pessoa Jurídica (PJ). Uma remessa = conversão de moeda (BRL → USD) + transferência do valor convertido, tudo em uma transação atômica.

## Stack

- **Java 21** (LTS)
- **Spring Boot 4.1.0** — atenção: nomes de starter modularizados (`spring-boot-starter-webmvc`, não `-web`; `spring-boot-h2console` para o console do H2)
- **Maven**
- **H2** em memória (`jdbc:h2:mem:remessadb;DB_CLOSE_DELAY=-1`) — sem servidor externo
- **Spring Data JPA** / Hibernate — `ddl-auto=create-drop`
- **Redis** — cache de cotação (diferencial, não prioridade agora)

Dependências deliberadamente **fora** do projeto: `spring-boot-starter-webservices` (é SOAP/Spring-WS, não tem relação com REST — foi removido do pom por não ter uso aqui).

## Arquitetura: Hexagonal (Ports & Adapters)

```
com.inter.remessa
 ├── config             → beans do Spring: RestClient/WebClient do BCB, config do Redis, etc.
 ├── domain
 │    ├── model        → Pessoa, PessoaFisica, PessoaJuridica, Wallet, Remessa, Money
 │    └── exception     → SaldoInsuficienteException, LimiteExcedidoException...
 ├── application
 │    ├── port
 │    │    ├── in       → RealizarRemessaUseCase (interface)
 │    │    └── out      → WalletRepositoryPort, CotacaoProviderPort, PessoaRepositoryPort
 │    ├── usecase       → RealizarRemessaService implements RealizarRemessaUseCase
 │    └── validator     → RemessaValidator (interface) + implementações
 └── adapter
      ├── in/web        → RemessaController, DTOs, @ExceptionHandler
      └── out
           ├── persistence → JPA repositories implementando os ports "out"
           ├── bcb         → CotacaoBcbAdapter (chama a API do BCB)
           └── cache       → CotacaoCacheDecorator (Redis)
```

Nomenclatura: `port.in`/`port.out` mantido como nome canônico do padrão original (Ports & Adapters, Alistair Cockburn) — é o vocabulário usado nos livros e nas implementações de referência (ex: buckpal), então sinaliza domínio do padrão pra quem já o conhece.

`config` fica no nível raiz porque é infraestrutura transversal do Spring — não pertence a `domain`, `application` nem `adapter`.

`validator` fica em `application`, não em `domain`, porque são `@Component` gerenciados pelo Spring que operam sobre o `Remessa` já montado (orquestração) — mesmo sendo regra de negócio em essência. Vale ter essa justificativa pronta se questionarem por que não está em `domain`.

Trade-off consciente: anotações JPA direto nas classes de `domain.model` (sem mapper separado) — hexagonal "puro" de livro seria mais isolado, mas não compensa o tempo extra pro escopo do desafio. Documentar essa decisão no README do projeto.

## Money (Value Object)

- Armazena valor internamente como **`long` em centavos** — soma/subtração/comparação são aritmética inteira, sem erro de arredondamento.
- **`BigDecimal` entra só na conversão de moeda** (cotação vem fracionária). `RoundingMode.HALF_EVEN` (arredondamento bancário) nesse único ponto.
- Construtor privado + factories (`ofReais`, `ofCents`) — nunca instanciar cru.
- `long` não estoura nem perto do limite pros valores em jogo (limite PJ é 50 mil/dia) — não precisa de `BigInteger`.

## Convenção: quando usar `record` (Java 21)

**Usar `record`:**
- DTOs da camada web (`RemessaRequest`, `RemessaResponse`)
- Command dos use cases (ex: `RealizarRemessaCommand(Long remetenteId, Long destinatarioId, BigDecimal valor)`)
- Mapeamento de resposta de API externa (ex: `CotacaoBcbResponse(List<CotacaoItem> value)`) — Jackson deserializa record nativamente
- `Money` — `record Money(long cents)` com factories estáticas e métodos de instância (`add`, `subtract`, `convert`). Ganho real: `equals`/`hashCode`/`toString` de graça (testes de igualdade funcionam sem esforço) e imutabilidade estrutural.

**NÃO usar `record`:**
- `Pessoa`/`PessoaFisica`/`PessoaJuridica`, `Wallet`, `Remessa` (são `@Entity` JPA — Hibernate espera identidade por ID, construtor sem argumentos, suporte a proxy/lazy loading; tudo isso conflita com a natureza de um record)
- Exceptions (record não pode estender `RuntimeException`)

## Pessoa / PessoaFisica / PessoaJuridica

- Herança JPA com `@Inheritance(strategy = InheritanceType.JOINED)` — tabela `pessoa` (nome, email, senha) + `pessoa_fisica` (cpf) + `pessoa_juridica` (cnpj).
- Tipo (PF/PJ) resolvido por **método abstrato** (`getTipo()`) na hierarquia — não por enum salvo à parte (evitaria inconsistência entre classe real e campo).
- Campo `nome` único na classe base cobre tanto "nome completo" (PF) quanto "razão social" (PJ), conforme o enunciado — documentar essa decisão no Javadoc/README.
- Senha: **hash** (bcrypt, via `spring-security-crypto`), nunca "criptografia" reversível.
- Unicidade: `email` único em `pessoa`; `cpf` único em `pessoa_fisica`; `cnpj` único em `pessoa_juridica`.

## Cotação de câmbio (API do BCB)

- Endpoint real: `https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia(dataCotacao=@dataCotacao)?@dataCotacao='MM-DD-YYYY'&$format=json`
  (o link do enunciado é só a página de documentação — o host real de chamada é `olinda.bcb.gov.br`, não `dadosabertos.bcb.gov.br`)
- Campo usado: `cotacaoCompra`.
- **Formato de data americano** (`MM-DD-AAAA`) — pegadinha fácil de errar.
- Resposta vem em array `value[]` (padrão OData) — acessar `value[0].cotacaoCompra`.
- Esse endpoint específico retorna um único valor por dia (fechamento PTAX, ~13h) — diferente do dataset "todos os boletins diários" (5 boletins/dia), que é outro recurso.
- **Fallback:** quando não houver cotação pra data pedida (fim de semana, feriado, ou consulta antes da publicação do dia), buscar a última cotação disponível andando pra trás — lógica genérica, não um `if (isWeekend())` hardcoded.
- Externalizar a base URL em `application.properties`, não deixar string solta no código (facilita mock em teste).

## Cache (Redis — diferencial)

- `CotacaoProviderPort` como interface; `CotacaoCacheDecorator` implementa a mesma interface e envolve o `CotacaoBcbAdapter` real (Decorator pattern).
- Cache por dia é seguro: uma vez publicada, a cotação de fechamento não muda mais naquele dia.
- Baixa prioridade — implementar só depois que a lógica de negócio central estiver funcionando e commitada.

## Fluxo da remessa (`RealizarRemessaService`)

Ordem importa por custo, não por regra de negócio arbitrária:

1. Buscar carteira do remetente (dado interno, sem custo externo)
2. Rodar todos os `RemessaValidator` (saldo suficiente, limite diário — ambos internos, **sem `@Order`**, são independentes entre si)
3. Só depois buscar a cotação (cache → API → fallback)
4. Converter o valor (**dividir** por `cotacaoCompra`, não multiplicar — `cotacaoCompra` = quantos reais por 1 dólar)
5. Débito/crédito nas carteiras, tudo dentro de `@Transactional` (rollback automático em qualquer falha)

Validators como Strategy: cada regra é um `@Component implements RemessaValidator` separado — nova regra de negócio = nova classe, sem tocar nas existentes (Open/Closed).

## Limites diários

- PF: R$ 10.000/dia
- PJ: R$ 50.000/dia
- Soma das remessas do dia, validado como dado interno (sem chamada externa)

## Repositório

- Nome sugerido: `desafio-inter-remessa`
- Privado, com acesso de leitura liberado pros avaliadores
- README.md com: como compilar/rodar, como rodar os testes, comentários livres (conforme pedido no enunciado) — incluir aqui as decisões de trade-off documentadas acima

## Convenção de desenvolvimento: TDD sempre

**Toda funcionalidade nova começa pelo teste, não pela implementação.** Ordem obrigatória:

1. Escrever o teste unitário que descreve o comportamento esperado (deve falhar — a classe/método ainda não existe ou está incompleto)
2. Escrever o código mínimo pra fazer esse teste passar
3. Refatorar se necessário, mantendo os testes verdes
4. Só então seguir pro próximo comportamento

Isso vale tanto pra `domain` (ex: `Money`, validators) quanto pra `application` (use cases) — inclusive os testes de integração dos adapters (`CotacaoBcbAdapter`, repositórios JPA) devem ser escritos descrevendo o comportamento esperado antes do adapter em si.

## Testes

- Testes unitários **e** testes de integração são item de avaliação — priorizar o core (Money, validators, RemessaService) antes dos diferenciais, mas sem pular a escrita do teste de integração quando o componente envolver algo externo (API do BCB, banco).
- Primeiro teste a escrever: direção da conversão de moeda (`R$ 543,00 / 5,43 = US$ 100,00`) — pega erro de inversão da fórmula na hora.

### Convenção de nomenclatura

- Idioma: **inglês** em tudo — nome do método, `@DisplayName`, variáveis locais do teste.
- Padrão do nome do método: `should<ExpectedBehavior>When<Condition>`.
- `@DisplayName` em inglês, descrevendo o comportamento em linguagem natural.

```java
@Test
@DisplayName("Should convert R$ 543.00 to US$ 100.00 when exchange rate is 5.43")
void shouldConvertReaisToDollarsWhenExchangeRateIsValid() { ... }
```

## Diferenciais (prioridade baixa, só se sobrar tempo)

- Testes de integração
- Docker (multi-stage build) + docker-compose (app + redis)
- Kubernetes manifests (deployment/service para app e redis, health check via Spring Actuator)

## Status atual

Setup do projeto (JDK 21, Spring Initializr, pom.xml) concluído. Próximo passo: escrever a classe `Money`.

