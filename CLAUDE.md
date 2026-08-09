# Desafio Técnico Java — Banco Inter (Serviço de Remessa)

Contexto do projeto para qualquer sessão do Claude Code. Este arquivo reflete decisões de design já fechadas em conversa anterior — use como ponto de partida, não como regra fixa. Se durante a implementação surgir um problema, risco ou alternativa melhor que essas decisões, **sempre apontar e sugerir a melhoria**, mesmo que contrarie o que está documentado aqui. Documentação desatualizada ou uma decisão que não se sustenta na prática vale mais ser corrigida do que seguida cegamente.

**Idioma:** este documento e as instruções ficam em português. Código (classes, métodos, variáveis, testes) fica em inglês, exceto os nomes de domínio já estabelecidos em português (`Pessoa`, `PessoaFisica`, `PessoaJuridica`, `Remessa`) — não fazem sentido bem traduzidos (PF/PJ, CPF/CNPJ não têm equivalente direto em inglês) e renomear agora quebraria código já escrito.

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
- **Factories por convenção, não por imposição da linguagem** (`ofReais`, `ofCents`) — como `Money` é um `record` público, o construtor canônico (`new Money(long)`) não pode ser `private`: Java exige que o construtor canônico tenha acesso pelo menos igual ao do próprio record. `new Money(x)` é tecnicamente alcançável de fora da classe; a convenção da equipe é sempre passar pelas factories. Trade-off aceito — fechar isso de vez significaria abrir mão do `record` (e do `equals`/`hashCode`/imutabilidade de graça) por uma classe comum com construtor privado de verdade, o que não compensa pro escopo do projeto.
- `long` não estoura nem perto do limite pros valores em jogo (limite PJ é 50 mil/dia) — não precisa de `BigInteger`.
- **Status: completa e testada** — 7 testes passando (`ofReais`, `add`, `subtract`, arredondamento na borda, `isLessThan` nos dois sentidos, `convert`).

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
- Campo `nome` único na classe base cobre tanto "nome completo" (PF) quanto "razão social" (PJ), conforme o enunciado — **ponto em aberto, ver "Pontos a discutir com avaliadores" abaixo.**
- **Senha**: hash (bcrypt, via `spring-security-crypto`), nunca "criptografia" reversível. **O hash acontece uma camada acima, no use case que cria a pessoa (ex: `CriarPessoaService`), não dentro de `Pessoa`/`PessoaFisica`/`PessoaJuridica`** — a entidade só guarda uma `String senhaHash` já pronta. Isso mantém o `domain` livre da dependência da biblioteca de segurança, consistente com a regra de isolamento de frameworks da arquitetura hexagonal. Os testes unitários de criação da entidade devem passar uma string já "parecendo hash" e só confirmar que foi armazenada — não é o lugar de testar hashing de verdade; isso fica pra um teste separado do use case que faz o hash.
- Unicidade: `email` único em `pessoa`; `cpf` único em `pessoa_fisica`; `cnpj` único em `pessoa_juridica`.
- **CPF**: 11 dígitos, só numérico. Guardado sem máscara. Formato validado no construtor com `java.util.regex.Pattern` puro (`^\d{11}$`), lançando `CpfInvalidoException` se não bater. Validação completa de dígito verificador (módulo 11) é diferencial, não é exigida pelo enunciado.
- **CNPJ**: 14 caracteres, guardado sem máscara. **Desde 31/07/2026 a Receita Federal começou a emitir CNPJs alfanuméricos** (Instrução Normativa RFB nº 2.229/2024) — as 12 primeiras posições agora podem ter letras maiúsculas ou números, só os 2 últimos (dígitos verificadores) continuam numéricos. CNPJs antigos (só números) continuam válidos indefinidamente; os dois formatos convivem. Formato validado no construtor com `^[A-Z0-9]{12}\d{2}$`, não `^\d{14}$` — uma checagem só-números rejeitaria incorretamente empresas recém-abertas. Algoritmo completo de dígito verificador (módulo 11 sobre valores ASCII-48) não implementado, já que o enunciado só pede unicidade, não validação de DV — vale mencionar aos avaliadores como uma mudança regulatória recente e fácil de passar batido.
- **Onde mora a validação de formato, e por quê:** os construtores das entidades validam com `java.util.regex.Pattern` puro (biblioteca padrão do JDK, não é dependência de framework) e lançam uma exceção de domínio — mantém o `domain` livre de framework, mesmo raciocínio da decisão de hash de senha acima, e garante entidades "sempre válidas" independente do ponto de entrada. Anotações `@Pattern`/Bean Validation são usadas separadamente nos DTOs da camada web (`PessoaRequest`, em `adapter/in/web`) pra uma rejeição HTTP 400 antecipada e apropriada pra essa camada — uma segunda camada de defesa, não substitui a validação no domínio.
- **Email**: formato validado no construtor com `java.util.regex.Pattern` puro (`^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`), lançando `EmailInvalidoException` se não bater — mesma abordagem do cpf/cnpj. **Normalizado pra minúsculo antes de guardar** (`email.toLowerCase()` no construtor) — do contrário, `User@test.com` e `user@test.com` poderiam se cadastrar como "únicos" apesar de serem, na prática, o mesmo endereço, furando silenciosamente a regra de unicidade do enunciado.
- **Status: em andamento** — testes de criação de `PessoaFisica`/`PessoaJuridica` passando; validação de formato (cpf/cnpj/email) sendo implementada.

**Unicidade (email/cpf/cnpj) — ainda não implementada, é o próximo passo depois da validação de formato.** Precisa de duas camadas: (1) no use case `CriarPessoaService` (ainda não existe), checar `pessoaRepositoryPort.existsByEmail(...)` antes de salvar e lançar uma exceção de negócio clara (`EmailJaCadastradoException`) — testável com repositório fake, sem precisar de banco real; (2) `@Column(unique = true)` na entidade JPA como rede de segurança contra condição de corrida — só um teste de integração contra o H2 real pega isso. É também o ponto onde `Pessoa` ganha as anotações `@Entity`/`@Inheritance` de verdade pela primeira vez.

## Pontos a discutir com avaliadores

Coisas que vale citar explicitamente na seção de decisões de design do README, ou trazer numa conversa com os avaliadores — ambiguidades do enunciado onde tomamos uma decisão pragmática, não necessariamente "a" resposta definitiva.

- **`nome` vs `razão social`.** O enunciado diz literalmente "nome completo" tanto pra PF quanto pra PJ, sem termo separado pro caso de empresa. Mas na linguagem real do domínio financeiro/jurídico brasileiro, uma empresa não tem "nome" — tem `razão social` (e muitas vezes um `nome fantasia` separado, que o enunciado não pede). Implementação atual: campo `nome` único na classe base `Pessoa`, reaproveitado pelos dois subtipos — opção mais barata, satisfaz o requisito ao pé da letra, mas perde essa distinção de domínio. **Vale melhorar se sobrar tempo:** adicionar um getter semântico em `PessoaJuridica` — `String getRazaoSocial() { return getNome(); }` — pra que o vocabulário da API/domínio bata com a terminologia financeira real, sem duplicar o campo nem mexer no mapeamento JPA. Documentar esse trade-off no README de qualquer forma — sinaliza consciência do domínio mesmo onde a implementação ficou simples.

## Cotação de câmbio (API do BCB)

- Endpoint real: `https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia(dataCotacao=@dataCotacao)?@dataCotacao='MM-DD-YYYY'&$format=json`
  (o link do enunciado é só a página de documentação — o host real de chamada é `olinda.bcb.gov.br`, não `dadosabertos.bcb.gov.br`)
- Campo usado: `cotacaoCompra`.
- **Formato de data americano** (`MM-DD-AAAA`) — pegadinha fácil de errar.
- Resposta vem em array `value[]` (padrão OData) — acessar `value[0].cotacaoCompra`.
- Esse endpoint específico retorna um único valor por dia (fechamento PTAX, ~13h) — diferente do dataset "todos os boletins diários" (5 boletins/dia), que é outro recurso.
- **Fallback:** quando não houver cotação pra data pedida (fim de semana, feriado, ou consulta antes da publicação do dia), buscar a última cotação disponível andando pra trás — lógica genérica, não um `if (isWeekend())` hardcoded. **Fica por último na ordem de implementação, conforme pedido explícito do enunciado.**
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
- Primeiro teste escrito: direção da conversão de moeda (`R$ 543,00 / 5,43 = US$ 100,00`) — já concluído, passou.

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

`Money` completa e testada (7 testes passando: `ofReais`, `add`, `subtract`, arredondamento, `isLessThan`, `convert`). `Pessoa`/`PessoaFisica`/`PessoaJuridica` em andamento: criação básica testada e implementada; validação de formato (cpf, cnpj, email) sendo adicionada nos construtores. Próximo passo: fechar essas validações e seguir pra `Wallet`.
