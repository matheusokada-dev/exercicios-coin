# Gestão de Numerário

Sistema para gestão de agências, saldos, movimentações e solicitações de
suprimento ou recolhimento de numerário.

A documentação HTML está organizada por porta no índice
[`docs/DOCUMENTACAO_COMPLETA.html`](docs/DOCUMENTACAO_COMPLETA.html).

## Projetos

| Projeto | Tecnologia | Porta | Responsabilidade |
| --- | --- | --- | --- |
| `api-numerario` | Java 21, Spring Boot 3.5, JPA, Flyway | 8081 | Domínio, casos de uso, segurança e persistência |
| `bff-numerario` | Java 21, Spring Boot 3.5 | 8080 | Contrato HTTP consumido pelo frontend |
| `relatorio-numerario` | Java 21, Spring Boot 3.5, Apache POI | 8082 | Microsserviço de relatórios de numerário |
| `frontend-numerario` | Angular 19, TypeScript | 4200 | Interface operacional |
| `database` | MySQL 8.4 | 3306 | Scripts auxiliares e documentação do banco |

O frontend chama o BFF por `/api`. O BFF encaminha as chamadas para a API, e a
API acessa o MySQL. API e BFF propagam `X-Correlation-ID`.

O Livro Caixa usa geração centralizada. O BFF consulta agência, sessão e todas
as páginas de movimentações na API, monta os dados tabulares e chama o
`relatorio-numerario`. A URL desse serviço é configurada por
`RELATORIOS_BASE_URL`; o frontend apenas converte o Base64 retornado em arquivo.
Por padrão, o serviço de relatórios escuta apenas em `127.0.0.1`. Em containers,
configure `RELATORIOS_ADDRESS=0.0.0.0` e restrinja a porta à rede interna.

## Configuração local

1. Copie `.env.example` para `.env`.
2. Preencha `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET`.
3. Use uma chave JWT aleatória com pelo menos 32 caracteres.
4. Não versione o arquivo `.env`.

O script `iniciar-tudo.ps1` carrega o `.env` no ambiente dos processos. Para
executar a API manualmente, exporte as mesmas quatro variáveis no terminal antes
do Maven. Não coloque credenciais em `src/main/resources`: tudo nesse diretório
é incluído no JAR.

O sistema utiliza UTC para persistência e contratos temporais. Campos que
representam apenas datas de calendário usam `LocalDate`; eventos usam `Instant`.
Valores monetários usam `BigDecimal` e `DECIMAL(19,2)`.

## Inicialização

Com MySQL disponível e as variáveis preenchidas:

```powershell
.\scripts\iniciar-tudo.ps1
```

Ordem de inicialização manual:

```powershell
cd api-numerario
.\mvnw.cmd spring-boot:run

cd ..\relatorio-numerario
..\api-numerario\mvnw.cmd -f .\pom.xml spring-boot:run

cd ..\bff-numerario
..\api-numerario\mvnw.cmd -f .\pom.xml spring-boot:run

cd ..\frontend-numerario
npm start
```

## Testes e builds

```powershell
cd api-numerario
.\mvnw.cmd test
.\mvnw.cmd package
.\mvnw.cmd verify

cd ..\bff-numerario
..\api-numerario\mvnw.cmd -f .\pom.xml test
..\api-numerario\mvnw.cmd -f .\pom.xml package
..\api-numerario\mvnw.cmd -f .\pom.xml verify

cd ..\relatorio-numerario
..\api-numerario\mvnw.cmd -f .\pom.xml test
..\api-numerario\mvnw.cmd -f .\pom.xml package

cd ..\frontend-numerario
npm ci
npx tsc -p tsconfig.app.json --noEmit --noUnusedLocals --noUnusedParameters
npm run build
```

O `verify` gera o relatório JaCoCo em `target/site/jacoco/index.html` na API e
no BFF e exige cobertura mínima de 90% de linhas.
O frontend não possui suíte automatizada no estado atual; a validação disponível
é a checagem estrita do TypeScript seguida do build de produção.

Os testes de persistência da API exigem MySQL com a V1 e, para os cenários
atuais, a massa local compatível. O ambiente atual não possui Docker; portanto, Testcontainers ainda
não está habilitado. Não execute essa suíte contra banco compartilhado ou de
produção.

## Banco e migrations

As migrations ficam em `api-numerario/src/main/resources/db/migration`.
A `V1__create_schema.sql` cria toda a estrutura. A massa de demonstração fica
fora do Flyway, em `database/scripts/seed-dados-dev.sql`, e deve ser aplicada
manualmente apenas em desenvolvimento, depois da V1, sobre um schema vazio.
Bancos criados pelo histórico V1–V6 devem ser recriados; não tente
aplicar a nova baseline sobre o histórico antigo. Depois do reset, toda evolução deve
receber uma nova versão Flyway sem alterar a baseline aplicada.

Por segurança, `FLYWAY_ENABLED` é `false` por padrão. Habilite-o somente ao
inicializar um schema local vazio ou durante uma promoção controlada.

O seed local contém credenciais conhecidas e nunca deve ser aplicado em
homologação ou produção:

```powershell
Get-Content .\database\scripts\seed-dados-dev.sql | mysql -u root -p gestao_numerario
```

## APIs

### Swagger / OpenAPI

| Serviço | Swagger UI | Especificação OpenAPI |
| --- | --- | --- |
| API | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/v3/api-docs` |
| BFF | `http://localhost:8080/swagger-ui.html` | `http://localhost:8080/v3/api-docs` |

A API e o BFF documentam autenticação Bearer JWT. O Angular armazena os tokens
no `localStorage` e envia o access token no header `Authorization`.
Defina `OPENAPI_ENABLED=false` nos ambientes em que Swagger UI e a especificação
não devam ficar expostos.

Todos os controllers públicos implementam uma interface `*Api`, que centraliza
a tag, o resumo e a descrição de cada operação.
As respostas comuns `400`, `401`, `403` e `500` são padronizadas; o BFF também
documenta `503` e `504` para falhas dos serviços dependentes. O login é
explicitamente público nas duas especificações.

Com API e BFF ativos, valide integralmente os contratos:

```powershell
node scripts/validar-openapi.mjs
```

O validador confere as 34 operações da API e as 32 do BFF, incluindo
`operationId`, tags, descrições, segurança e respostas HTTP.

O padrão de falhas, códigos estáveis e mensagens de validação está documentado
em `docs/ERROS_API.md`.

### Autenticação do navegador

O Angular armazena o access token e o resumo da sessão nas chaves
`coin.accessToken` e `coin.sessao` do `localStorage`.
O interceptor adiciona `Authorization: Bearer <token>` às chamadas protegidas.
Como os tokens ficam acessíveis ao JavaScript, a aplicação deve manter proteção
rigorosa contra XSS e evitar scripts de terceiros não confiáveis.

O access token dura 8 horas. Não existe refresh token; ao expirar ou receber
`401 Unauthorized`, o frontend limpa a sessão local e solicita novo login.

Endpoints do navegador:

- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`

O schema atual não possui tabela de refresh token.

- `/api/v1/agencias` e `/api/v1/solicitacoes`: contratos existentes.
- `GET /api/v1/movimentacoes`: consulta do razão usada pelo Livro Caixa.
- `POST /api/v1/relatorios/livro-caixa`: geração centralizada do Livro Caixa.
- `/api/v1/solicitacoes-numerario`, `/api/v1/operacoes-numerario` e
  `/api/v1/unidades-operacionais`: evolução do fluxo de numerário sem criar uma
  segunda versão da API.

Consulte `docs/CONTRATOS_API.md`, `docs/REGRAS_SOLICITACAO_NUMERARIO.md` e
`docs/ARQUITETURA.md` para os contratos e regras detalhados.
