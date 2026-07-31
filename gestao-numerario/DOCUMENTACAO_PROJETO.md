# Gestão de Numerário

Este arquivo é o ponto de entrada da documentação do projeto. Para contexto de
execução e retomada, consulte [docs/LEIA_PRIMEIRO.md](docs/LEIA_PRIMEIRO.md).
A documentação navegável começa no índice
[docs/DOCUMENTACAO_COMPLETA.html](docs/DOCUMENTACAO_COMPLETA.html) e está
separada por aplicação:

- [Frontend — porta 4200](docs/DOCUMENTACAO_FRONTEND_4200.html)
- [BFF — porta 8080](docs/DOCUMENTACAO_BFF_8080.html)
- [API — porta 8081](docs/DOCUMENTACAO_API_8081.html)
- [Relatórios — porta 8082](docs/DOCUMENTACAO_RELATORIO_8082.html)

Cada HTML descreve o fluxo completo da porta e cataloga as classes, arquivos,
dependências, mappings e métodos do respectivo projeto.

## Estado atual

- API em Java 21, Spring Boot 3.5.14, Maven e arquitetura hexagonal, na porta
  `8081`.
- BFF Spring Boot na porta `8080`, responsável por encaminhar os contratos
  `/api/v1` para a API.
- Serviço centralizado de relatórios na porta `8082`, responsável por receber
  dados tabulares e devolver arquivos `.xlsx` em Base64.
- Frontend Angular na porta `4200`, consumindo somente o BFF.
- MySQL 8.4 com `V1__create_schema.sql` para estrutura e
  `database/scripts/seed-dados-dev.sql` para a massa local opcional.
- A nova baseline pressupõe schema vazio, não cria `unidade_operacional` nem
  tabela de refresh token.
- Autenticação stateless com access token JWT Bearer válido por 8 horas.
- O navegador guarda `coin.accessToken` e `coin.sessao` no `localStorage`.
- Não existem renovação automática, refresh token, cookies de autenticação ou
  endpoint de refresh.
- Cinco tentativas de login inválidas bloqueiam o usuário por 15 minutos.
- Operações financeiras usam controle otimista, transação, histórico e
  idempotência.

## Inicialização local

1. Inicie o MySQL e confirme que `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e
   `JWT_SECRET` estão configurados.
2. Para um banco recém-resetado, habilite `FLYWAY_ENABLED=true` na primeira
   inicialização da API para aplicar a V1.
3. Depois da criação do schema, volte `FLYWAY_ENABLED=false`.
4. Somente em desenvolvimento, aplique manualmente
   `database/scripts/seed-dados-dev.sql` para criar os cenários locais.
5. Inicie API, serviço de relatórios, BFF e frontend, nessa ordem.

O script `scripts/iniciar-tudo.ps1` automatiza a inicialização dos quatro
processos, mas não recria o banco.

## Portas

| Componente | Porta | Endereço principal |
| --- | ---: | --- |
| Frontend | 4200 | `http://localhost:4200` |
| BFF | 8080 | `http://localhost:8080` |
| API | 8081 | `http://localhost:8081` |
| Serviço de relatórios | 8082 | `http://localhost:8082` |

## OpenAPI

| Serviço | Swagger UI | JSON OpenAPI |
| --- | --- | --- |
| BFF | `http://localhost:8080/swagger-ui.html` | `http://localhost:8080/v3/api-docs` |
| API | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/v3/api-docs` |

Todos os controllers implementam uma interface `*Api`, responsável pela tag,
resumo e descrição das operações. Os contratos gerados também possuem
identificador, respostas comuns e indicação de segurança. Execute
`node scripts/validar-openapi.mjs` com API e BFF ativos para auditar os dois
contratos.

## Documentos

- [Índice HTML por porta](docs/DOCUMENTACAO_COMPLETA.html)
- [Frontend — porta 4200](docs/DOCUMENTACAO_FRONTEND_4200.html)
- [BFF — porta 8080](docs/DOCUMENTACAO_BFF_8080.html)
- [API — porta 8081](docs/DOCUMENTACAO_API_8081.html)
- [Relatórios — porta 8082](docs/DOCUMENTACAO_RELATORIO_8082.html)
- [Contexto e execução](docs/LEIA_PRIMEIRO.md)
- [Arquitetura](docs/ARQUITETURA.md)
- [Requisitos](docs/REQUISITOS.md)
- [Modelo de dados](docs/MODELO_DADOS.md)
- [Contratos da API e do BFF](docs/CONTRATOS_API.md)
- [Regras da solicitação de numerário](docs/REGRAS_SOLICITACAO_NUMERARIO.md)
- [Erros da API](docs/ERROS_API.md)
- [Massa local de desenvolvimento](docs/MASSA_DADOS.md)
- [Padrão visual do frontend](docs/PADRAO_FRONTEND_BRADESCO.md)
- [Diário histórico](docs/DIARIO_IMPLEMENTACAO.md)

## Manutenção

A V1 é a baseline consolidada para o reset atual. Depois que ela for aplicada
em um ambiente compartilhado, não deve ser alterada; toda evolução posterior
deve receber uma nova versão Flyway.

O diário registra decisões e estados históricos e pode mencionar estruturas
anteriores. Documentos operacionais, README e documentação consolidada devem
sempre descrever o estado executável atual.
