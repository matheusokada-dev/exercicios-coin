# Modelo de dados

## Fonte e adaptação

O modelo vem do guia técnico e foi adaptado de SQL Server para MySQL 8.4.

| Conceito do guia | Adaptação MySQL |
| --- | --- |
| `BIGINT IDENTITY(1,1)` | `BIGINT AUTO_INCREMENT` |
| `BIT` | `BOOLEAN` |
| `DATETIME2` + `SYSUTCDATETIME()` | `DATETIME(6)` + `UTC_TIMESTAMP(6)` |
| Índice filtrado por `WHERE` | coluna gerada armazenada + índice único |

## Convenções confirmadas

- Valores monetários: `DECIMAL(19,2)`.
- Datas técnicas: UTC.
- IDs: `BIGINT` auto incrementáveis.
- Senhas: somente `senha_hash`; nunca senha em texto puro.
- Controle otimista: campo `versao` em entidades mutáveis.
- Movimentações são imutáveis; nenhuma operação comum de update ou delete será criada para elas.
- Agências usadas em histórico são desativadas por `ativo`, não apagadas fisicamente.

## Migrations

- `V1__create_schema.sql`: cria tabelas iniciais, constraints, índices e regra de solicitação aberta por agência.
- `V2__adiciona_direcao_movimentacao.sql`: adiciona direção da movimentação para permitir ajuste de entrada ou saída.
- `V3__adiciona_controle_tentativas_login.sql`: adiciona contador persistente de falhas e bloqueio temporário de login.

## Tabelas

### `usuario`

Campos principais: `id`, `nome`, `login`, `senha_hash`, `perfil`, `ativo`, `criado_em`, `tentativas_login_falhas`, `bloqueado_ate`.

Regras:

- `login` é único.
- `perfil` aceita `OPERADOR` ou `GESTOR`.
- Senha nunca é retornada pela API.
- Cinco senhas incorretas consecutivas bloqueiam novas autenticações por 15 minutos.
- Uma autenticação válida zera o contador e o bloqueio; o controle é persistido para sobreviver à reinicialização da API.

### `agencia`

Campos principais: `id`, `codigo`, `nome`, `cidade`, `saldo_atual`, `limite_minimo`, `ativo`, `versao`, `criado_em`, `atualizado_em`.

Regras:

- `codigo` é único.
- `saldo_atual` e `limite_minimo` não podem ser negativos.
- Remoção funcional é desativação lógica por `ativo`.

### `solicitacao_abastecimento`

Campos principais: `id`, `agencia_id`, `valor`, `motivo`, `data_desejada`, `status`, `solicitante_id`, `decisor_id`, `justificativa_decisao`, `justificativa_especial`, `data_criacao`, `data_decisao`, `data_atendimento`, `versao`.

Regras:

- `valor` deve ser maior que zero.
- `status` aceita `PENDENTE`, `APROVADA`, `REJEITADA` ou `ATENDIDA`.
- Transições válidas: `PENDENTE -> APROVADA | REJEITADA` e `APROVADA -> ATENDIDA`.
- Uma agência só pode ter uma solicitação aberta (`PENDENTE` ou `APROVADA`).

### `movimentacao`

Campos principais: `id`, `agencia_id`, `solicitacao_id`, `tipo`, `valor`, `saldo_anterior`, `saldo_posterior`, `descricao`, `data_movimento`, `usuario_id`, `idempotency_key`, `entrada`.

Tipos:

- `ABASTECIMENTO`
- `RECOLHIMENTO`
- `SAQUE`
- `DEPOSITO`
- `AJUSTE`

Regras:

- `valor` deve ser maior que zero.
- `idempotency_key` evita repetição de operações.
- `ABASTECIMENTO` é criado pelo atendimento de solicitação.
- `DEPOSITO` soma saldo.
- `SAQUE` e `RECOLHIMENTO` subtraem saldo e não podem deixar saldo negativo.
- `AJUSTE` usa `entrada` para decidir se soma ou subtrai.

## Índices principais

- Agência: busca por ativo e indicadores de alerta.
- Movimentação: consulta por agência/data e tipo/data.
- Solicitação: consulta por status/data e agência/status.
- Solicitação aberta: índice único sobre coluna gerada armazenada, conforme D01.
