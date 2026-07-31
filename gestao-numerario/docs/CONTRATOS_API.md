# Contratos da API e do BFF

> A especificação executável é publicada por cada serviço em `/v3/api-docs` e
> `/swagger-ui.html`. Este documento registra decisões que não devem depender
> apenas da geração automática.

## Governança OpenAPI

- Todo `@RestController` da API e do BFF deve implementar uma interface de
  contrato `*Api`.
- A interface `*Api` concentra o `@Tag` da área e um `@Operation`, com
  `summary` e `description`, para cada operação pública.
- O controller mantém os mapeamentos Spring MVC e a implementação, sem
  duplicar anotações funcionais do OpenAPI.
- API e BFF publicam o esquema `bearerAuth` do tipo HTTP bearer/JWT.
- `POST /api/v1/auth/login` declara segurança vazia porque é a única operação
  pública.
- Operações autenticadas documentam, no mínimo, respostas `400`, `401`, `403`
  e `500`.
- O BFF também documenta `503` e `504`, pois depende da API e do serviço de
  relatórios.
- Respostas específicas de negócio, como `404`, `409` e `422`, permanecem nos
  contratos das jornadas correspondentes.

O padrão `interface *Api` → `Controller implements *Api` é obrigatório para
novos endpoints e alterações dos contratos existentes. O `OpenApiConfig`
permanece responsável somente por elementos transversais, como segurança e
respostas técnicas compartilhadas.

Cobertura executável atual:

| Serviço | Controllers | Operações | Tags | Schemas |
| --- | ---: | ---: | ---: | ---: |
| API | 10 | 34 | 10 | 37 |
| BFF | 7 | 32 | 7 | 37 |

Validação:

```powershell
node scripts/validar-openapi.mjs
```

O script consulta os dois `/v3/api-docs` e falha quando uma operação não possui
resumo, descrição, `operationId`, tag, segurança ou respostas obrigatórias.

## Compatibilidade de erros

Respostas da API mantêm `codError`, `msgError` e `value` para consumidores
legados. Os campos preferidos para novas integrações são `code`, `message`,
`path`, `timestamp` e `fields`.

Validações devem informar o campo e a causa concreta. Consulte
`docs/ERROS_API.md` para o catálogo e exemplos.

## Controle otimista

Campos `versao`, `versaoOperacao`, `versaoSolicitacao` e `versaoUnidade` são
obrigatórios nos comandos que alteram recursos versionados. Omiti-los produz
`400 Bad Request`; informar uma versão defasada produz `409 Conflict` com o
código `CONFLITO_VERSAO`.

## Situação

A API usa base `/api/v1` e o BFF mantém o mesmo prefixo para simplificar o consumo pelo Angular. O frontend chama rotas relativas, por exemplo `/api/v1/auth/login`, e o BFF encaminha para a API.

## Convenções adotadas

- Base: `/api/v1`.
- Datas em ISO-8601 e UTC.
- Valores monetários como número JSON, nunca formatados como moeda.
- Erros em formato único com código, mensagem, status e campo quando aplicável.
- Paginação para listas de agências, solicitações e movimentações.
- Todas as rotas, exceto login, exigem `Authorization: Bearer <accessToken>`.
- IDs de usuário autenticado não devem ser enviados pelo frontend quando a API já puder extraí-los do token.

## Autenticação

| Método e rota | Corpo | Resposta |
| --- | --- | --- |
| `POST /api/v1/auth/login` | `login`, `senha` | `200 OK` + access token e dados da sessão |
| `GET /api/v1/auth/me` | Header Bearer | Dados resumidos da sessão |

O access token é JWT Bearer com validade de 8 horas e fica no `localStorage`.
Não existe renovação automática: ao expirar, o usuário deve autenticar novamente.

Login e senha são obrigatórios. Cinco senhas incorretas consecutivas bloqueiam a conta por 15 minutos, e uma autenticação válida limpa o contador. Usuário inexistente, inativo, bloqueado ou senha incorreta recebem o mesmo status `401` e a mesma mensagem principal.

Quando a conta existe e está ativa, o erro `401` inclui em `value` os campos `tentativasRestantes` e `bloqueadoAte`. O frontend usa esses dados para avisar quantas tentativas faltam ou o horário de liberação do acesso.

## Dashboard

| Método e rota | Corpo | Resposta |
| --- | --- | --- |
| `GET /api/v1/dashboard` | nenhum | `200 OK` + resumo operacional da data UTC atual |

O resumo contém `numerarioTotal`, `quantidadeAgenciasEmAlerta`, `quantidadeSolicitacoesPendentes`, `quantidadeAbastecimentosHoje`, `valorAbastecidoHoje` e `dataReferencia`.

## Agências

Base: `/api/v1/agencias`.

| Método e rota | Corpo | Resposta |
| --- | --- | --- |
| `POST /api/v1/agencias` | `codigo`, `nome`, `cidade`, `saldoAtual`, `limiteMinimo` | `201 Created` + agência |
| `GET /api/v1/agencias` | filtros opcionais `busca`, `ativo`, `alerta`, `ordenarPor`, `direcao`, `pagina`, `tamanho` | `200 OK` + página de agências |
| `GET /api/v1/agencias/{id}` | nenhum | `200 OK` + agência |
| `GET /api/v1/agencias/{id}/detalhe` | nenhum | `200 OK` + indicadores operacionais da agência |
| `PUT /api/v1/agencias/{id}` | `nome`, `cidade`, `limiteMinimo` | `200 OK` + agência atualizada |
| `DELETE /api/v1/agencias/{id}` | nenhum | `204 No Content`; desativação lógica |

O retorno inclui `abaixoDoLimite` e `sugestaoAbastecimento`, derivados das regras do domínio. A lista suporta pesquisa por código, nome ou cidade. O detalhe retorna entradas e saídas do dia UTC, além de `saldoPrevistoAposAbastecimentoAprovado`.

## Solicitações de abastecimento

Base: `/api/v1/solicitacoes`.

| Método e rota | Corpo | Resposta |
| --- | --- | --- |
| `POST /api/v1/solicitacoes` | `agenciaId`, `valor`, `motivo`, `dataDesejada` | `201 Created` + solicitação pendente |
| `GET /api/v1/solicitacoes` | filtros opcionais `agenciaId`, `status`, `dataInicio`, `dataFim`, `pagina`, `tamanho` | `200 OK` + página de solicitações |
| `PUT /api/v1/solicitacoes/{id}/aprovar` | `justificativaDecisao`, `justificativaEspecial` opcional | `200 OK` + solicitação aprovada |
| `PUT /api/v1/solicitacoes/{id}/rejeitar` | `justificativaDecisao` | `200 OK` + solicitação rejeitada |
| `PUT /api/v1/solicitacoes/{id}/atender` | `idempotencyKey` | `200 OK` + solicitação atendida |

O usuário solicitante, decisor ou atendente deve ser obtido do token nas rotas já adaptadas para autenticação. A `idempotencyKey` é obrigatória no atendimento.

## Usuários

Base: `/api/v1/usuarios`.

| Método e rota | Corpo | Resposta |
| --- | --- | --- |
| `POST /api/v1/usuarios` | `nome`, `login`, `senha`, `perfil` (`OPERADOR` ou `GESTOR`) | `201 Created` + usuário sem senha/hash |
| `GET /api/v1/usuarios/{id}` | nenhum | `200 OK` + usuário sem senha/hash |

A senha só é recebida na criação e é persistida com hash BCrypt. Ela nunca integra a resposta da API.

## Movimentações

Base: `/api/v1/movimentacoes`.

| Método e rota | Corpo | Resposta |
| --- | --- | --- |
| `GET /api/v1/movimentacoes` | filtros opcionais `agenciaId`, `tipo`, `dataInicio`, `dataFim`, `pagina`, `tamanho` | `200 OK` + página de movimentações |

Movimentações não são lançadas manualmente. Elas são criadas pelos fluxos de
solicitação, operação, carga inicial ou ajuste financeiro e permanecem imutáveis.

### Uso no Livro Caixa

O frontend consulta `GET /api/v1/agencias` para a seleção e solicita a geração
por `POST /api/v1/relatorios/livro-caixa`:

```json
{
  "agenciaId": 1,
  "dataInicio": "2026-07-01",
  "dataFim": "2026-07-29"
}
```

O BFF pagina `GET /api/v1/movimentacoes` com `tamanho=100`, preserva as colunas
do Livro Caixa, calcula os totalizadores e envia os dados tabulares e metadados
para `POST /v1/relatorios/gerar` no `relatorio-numerario`. A resposta do BFF
mantém `conteudo`, `nomeArquivo`, `formato` e `dataGeracao`; o Angular converte
`conteudo`, em Base64, para download.

### Serviço centralizado de relatórios

Em desenvolvimento local, o `relatorio-numerario` atende em
`http://localhost:8082`. O BFF usa `RELATORIOS_BASE_URL` para permitir a troca
desse endereço por ambiente.

Contrato enviado pelo BFF:

```json
{
  "colunas": ["Data e hora", "Tipo", "Entrada", "Saída"],
  "linhas": [
    ["29/07/2026 10:30:00", "ABASTECIMENTO", 1500.00, ""]
  ],
  "metadados": {
    "titulo": "Relatório Livro Caixa",
    "subtitulo": "Agência: 0001 - Agência Centro",
    "periodo": "01/07/2026 - 29/07/2026",
    "usuario": "Gestor",
    "nomeArquivo": "livro-caixa-0001-2026-07-01-2026-07-29"
  },
  "formato": "xlsx"
}
```

Resposta:

```json
{
  "conteudo": "UEsDB...",
  "nomeArquivo": "livro-caixa-0001-2026-07-01-2026-07-29.xlsx",
  "formato": "xlsx",
  "dataGeracao": "2026-07-29T16:54:05"
}
```

Cada linha deve possuir exatamente a mesma quantidade de células declarada em
`colunas`. Números são enviados como valores JSON numéricos e datas já
formatadas como texto pelo BFF. O gerador utiliza Java e Apache POI; não existe
biblioteca de planilhas no frontend.

## BFF

O BFF mantém as rotas `/api/v1` e encaminha o JWT recebido do frontend para a API.

| Área | Rotas implementadas no BFF | Status |
| --- | --- | --- |
| Autenticação | `POST /api/v1/auth/login` e `GET /me` | Implementado |
| Dashboard | `GET /api/v1/dashboard` | Em andamento |
| Agências | `GET /api/v1/agencias`, `GET /api/v1/agencias/{id}/detalhe`, `POST`, `PUT`, `DELETE` | Em andamento |
| Solicitações | `GET`, `POST`, `PUT /api/v1/solicitacoes/{id}/{acao}` | Em andamento |
| Movimentações | `GET` | Implementado para Livro Caixa e auditoria |
| Relatórios | `POST /api/v1/relatorios/livro-caixa` | Implementado via `relatorio-numerario` |

### BFF — fluxo evoluído de Tesouraria

O BFF expõe os mesmos caminhos públicos da API v1, mantém DTOs próprios e
repassa o JWT. Nos comandos idempotentes, o cabeçalho `Idempotency-Key` recebido
do frontend é preservado.

| Área | Rotas |
| --- | --- |
| Solicitações | `GET/POST /api/v1/solicitacoes-numerario`, `GET /{id}`, `GET /{id}/historico`, `PUT /{id}/aprovar`, `/rejeitar` e `/cancelar` |
| Logística | `PUT /{id}/programar`, `/iniciar-separacao`, `/expedir`, `/registrar-ocorrencia`, `/receber` e `/conciliar` |
| Consultas auxiliares | `GET /api/v1/unidades-operacionais`, `GET /api/v1/operacoes-numerario` |
| Financeiro | `POST /api/v1/tesouraria/carga-inicial`, `POST /api/v1/solicitacoes-numerario/{id}/ajustes-divergencia` |

O BFF preserva o status HTTP, o tipo de conteúdo e o corpo de erros devolvidos
pela API, inclusive `403 Forbidden`, `409 Conflict` e `410 Gone`.

## Pendência

Antes de concluir cada área, validar manualmente permissões, payloads, respostas de erro e compatibilidade entre frontend, BFF e API. Nesta etapa, testes e compilações automatizadas não serão executados por decisão do usuário.
