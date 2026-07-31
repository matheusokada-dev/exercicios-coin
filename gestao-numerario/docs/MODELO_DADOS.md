# Modelo de dados

O modelo executável é definido pelas migrations em
`api-numerario/src/main/resources/db/migration`.

## Migrations

- `V1__create_schema.sql`: cria todas as tabelas, índices, chaves e restrições.
- `database/scripts/seed-dados-dev.sql`: popula manualmente a massa
  determinística exclusiva de desenvolvimento; não faz parte do Flyway.

As duas migrations pressupõem um schema vazio. Não aplique esta nova baseline
sobre um banco que possua o histórico Flyway antigo.

## Tabelas

### `usuario`

Usuários de perfil `GESTOR` ou `OPERADOR`, com senha BCrypt, ativação e controle
de tentativas de login.

### `agencia`

Cadastro e posição financeira da agência. Os campos centrais são `codigo`,
`nome`, `cidade`, `saldo_atual`, `limite_minimo`, `ativo` e `versao`.

O saldo pertence diretamente à agência. Não existe tabela, coluna obrigatória
ou chave estrangeira de unidade operacional. Por isso o formulário de cadastro
cria uma agência apenas com os dados que efetivamente exibe.

Uma agência ativa está em alerta quando `saldo_atual < limite_minimo`.

### `solicitacao_numerario`

Solicitação de `SUPRIMENTO` ou `RECOLHIMENTO`, ligada diretamente à agência.
Suporta `PENDENTE`, `APROVADA`, `REJEITADA`, `EM_EXECUCAO`, `CONCLUIDA`,
`CANCELADA` e `COM_DIVERGENCIA`.

A rota é persistida diretamente pelas colunas opcionais `origem_agencia_id` e
`destino_agencia_id`. Ao criar um suprimento, o destino é a agência solicitante;
ao criar um recolhimento, ela é a origem. A programação escolhe a outra agência.

A coluna gerada `agencia_aberta_id` garante no banco no máximo uma solicitação
aberta por agência.

### `operacao_numerario`

Execução logística da solicitação. Suporta `PROGRAMADA`, `EM_SEPARACAO`,
`EM_TRANSITO`, `RECEBIDA`, `CONCILIADA` e `COM_DIVERGENCIA`.

Origem e destino são agências distintas e ficam congeladas na operação por
`origem_agencia_id` e `destino_agencia_id`. A expedição debita o saldo da origem
e o recebimento credita o valor efetivamente recebido no destino.

### `movimentacao`

Livro-caixa ligado diretamente à agência, com direção, valor, saldo anterior e
posterior, usuário, solicitação/operação opcionais e idempotência.

### `historico_solicitacao_numerario`

Linha do tempo auditável das solicitações e operações.

### `comando_idempotente`

Registro de comandos processados para impedir repetição de efeitos financeiros.

## Estruturas removidas

O reset não recria `unidade_operacional`, sessões de refresh token nem as
migrations incrementais legadas V3–V6.
