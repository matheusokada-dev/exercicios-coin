# Contratos da API e do BFF

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
| `POST /api/v1/auth/login` | `login`, `senha` | `200 OK` + `accessToken`, `tokenType`, `expiraEm` |

O token é JWT Bearer com validade de 60 minutos, sem refresh token. O primeiro gestor já foi criado diretamente no MySQL. Depois disso, criação de usuários é fluxo protegido por perfil `GESTOR`.

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
| `POST /api/v1/movimentacoes` | `agenciaId`, `tipo`, `valor`, `descricao`, `idempotencyKey`, `entradaAjuste` quando tipo for `AJUSTE` | `201 Created` + movimentação |
| `GET /api/v1/movimentacoes` | filtros opcionais `agenciaId`, `tipo`, `dataInicio`, `dataFim`, `pagina`, `tamanho` | `200 OK` + página de movimentações |

Os tipos manuais são `SAQUE`, `DEPOSITO`, `RECOLHIMENTO` e `AJUSTE`. `ABASTECIMENTO` é exclusivo do atendimento de uma solicitação aprovada. Para `AJUSTE`, `entradaAjuste` indica se a correção soma ou subtrai valor.

### Uso no Livro Caixa

O frontend gera o Livro Caixa sem endpoint adicional. Ele consulta `GET /api/v1/agencias` para a seleção e pagina `GET /api/v1/movimentacoes` com `agenciaId`, `dataInicio`, `dataFim`, `pagina` e `tamanho=100`. O XLSX é montado no navegador.

## BFF

O BFF mantém as rotas `/api/v1` e encaminha o JWT recebido do frontend para a API.

| Área | Rotas implementadas no BFF | Status |
| --- | --- | --- |
| Autenticação | `POST /api/v1/auth/login` | Em andamento |
| Dashboard | `GET /api/v1/dashboard` | Em andamento |
| Agências | `GET /api/v1/agencias`, `GET /api/v1/agencias/{id}/detalhe`, `POST`, `PUT`, `DELETE` | Em andamento |
| Solicitações | `GET`, `POST`, `PUT /api/v1/solicitacoes/{id}/{acao}` | Em andamento |
| Movimentações | `GET`, `POST` | Em andamento |

## Pendência

Antes de concluir cada área, validar manualmente permissões, payloads, respostas de erro e compatibilidade entre frontend, BFF e API. Nesta etapa, testes e compilações automatizadas não serão executados por decisão do usuário.
