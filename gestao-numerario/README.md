# Gestão de Numerário

Sistema para gestão de agências, saldos, movimentações e solicitações de
suprimento ou recolhimento de numerário.

## Projetos

| Projeto | Tecnologia | Porta | Responsabilidade |
| --- | --- | --- | --- |
| `api-numerario` | Java 21, Spring Boot 3.5, JPA, Flyway | 8081 | Domínio, casos de uso, segurança e persistência |
| `bff-numerario` | Java 21, Spring Boot 3.5 | 8080 | Contrato HTTP consumido pelo frontend |
| `frontend-numerario` | Angular 19, TypeScript | 4200 | Interface operacional |
| `database` | MySQL 8.4 | 3306 | Scripts auxiliares e documentação do banco |

O frontend chama o BFF por `/api`. O BFF encaminha as chamadas para a API, e a
API acessa o MySQL. API e BFF propagam `X-Correlation-ID`.

## Configuração local

1. Copie `.env.example` para `.env`.
2. Preencha `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET`.
3. Use uma chave JWT aleatória com pelo menos 32 caracteres.
4. Não versione o arquivo `.env`.

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

cd ..\frontend-numerario
npm test -- --watch=false --browsers=ChromeHeadless
npm test -- --watch=false --browsers=ChromeHeadless --code-coverage
npm run build
```

O `verify` gera o relatório JaCoCo em `target/site/jacoco/index.html` nos dois
projetos Java e exige cobertura mínima de 90% de linhas.
O teste Angular com `--code-coverage` gera o relatório em
`coverage/frontend-numerario/index.html`.

Os testes de persistência da API exigem MySQL com as migrations V1–V6 e massa
compatível. O ambiente atual não possui Docker; portanto, Testcontainers ainda
não está habilitado. Não execute essa suíte contra banco compartilhado ou de
produção.

## Banco e migrations

As migrations ficam em `api-numerario/src/main/resources/db/migration`.
Migrations aplicadas não devem ser alteradas. Toda evolução deve receber uma
nova versão Flyway.

Por segurança, `FLYWAY_ENABLED` é `false` por padrão. A V4 altera estruturas
existentes e só deve ser promovida após backup validado. Use o procedimento
descrito em `docs/MIGRACAO_BANCO_SEGURA.md` e o script
`scripts/migrar-banco-seguro.ps1`.

Scripts em `database/scripts` são exclusivos para desenvolvimento local e
contêm credenciais conhecidas de massa de teste, nunca credenciais corporativas.

## APIs

### Swagger / OpenAPI

| Serviço | Swagger UI | Especificação OpenAPI |
| --- | --- | --- |
| API | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/v3/api-docs` |
| BFF | `http://localhost:8080/swagger-ui.html` | `http://localhost:8080/v3/api-docs` |

A API e o BFF documentam autenticação Bearer JWT. O Angular armazena os tokens
no `localStorage` e envia o access token no header `Authorization`.

O padrão de falhas, códigos estáveis e mensagens de validação está documentado
em `docs/ERROS_API.md`.

### Autenticação do navegador

O Angular armazena access token, refresh token e o resumo da sessão nas chaves
`coin.accessToken`, `coin.refreshToken` e `coin.sessao` do `localStorage`.
O interceptor adiciona `Authorization: Bearer <token>` às chamadas protegidas.
Como os tokens ficam acessíveis ao JavaScript, a aplicação deve manter proteção
rigorosa contra XSS e evitar scripts de terceiros não confiáveis.

O access token dura 15 minutos e o refresh token, 8 horas. Refresh tokens são
aleatórios e rotativos, e somente o hash SHA-256 é persistido no MySQL. Um novo
login ou o bloqueio do usuário revoga a sessão anterior.

Endpoints do navegador:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

O schema é criado incrementalmente por `V5__cria_sessoes_refresh.sql`.

- `/api/v1/agencias`, `/api/v1/movimentacoes`, `/api/v1/solicitacoes`:
  contratos existentes, mantidos para compatibilidade.
- `/api/v1/solicitacoes-numerario`, `/api/v1/operacoes-numerario` e
  `/api/v1/unidades-operacionais`: evolução do fluxo de numerário sem criar uma
  segunda versão da API.

Consulte `docs/CONTRATOS_API.md`, `docs/REGRAS_SOLICITACAO_NUMERARIO.md` e
`docs/ARQUITETURA.md` para os contratos e regras detalhados.
