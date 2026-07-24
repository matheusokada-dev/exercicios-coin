# Massa de dados de desenvolvimento

Atualizado em: 23/07/2026.

## Escopo atual

A massa cobre somente as tabelas ja implementadas:

- `usuario`
- `agencia`
- `solicitacao_abastecimento`
- `movimentacao`

Cliente, Ponto, Cofre e Frequencia ficam fora desta fase porque ainda nao existem no modelo.

## Script

Arquivo executavel:

`database/scripts/seed-dados-dev.sql`

O script e exclusivo para desenvolvimento local, deterministico e idempotente. Ele pode ser reaplicado sem duplicar os registros identificados pela massa.

Todos os usuarios criados pelo script usam a senha local `admin123`. Essa credencial nao deve ser usada em homologacao ou producao.

## Composicao esperada

### Usuarios

Sete usuarios identificados pelo seed:

- Dois gestores ativos.
- Quatro operadores ativos.
- Um operador inativo para teste de acesso.

### Agencias

Trinta agencias com codigos entre `0101` e `0130`:

- 26 ativas.
- 4 inativas.
- 11 ativas abaixo do limite minimo.
- 2 ativas exatamente no limite minimo.
- Demais agencias acima do limite.
- Cidades distribuidas por diferentes regioes do Brasil.

### Solicitacoes

176 solicitacoes identificadas pelo prefixo `[SEED-MASSA-V1-`:

| Status | Quantidade |
| --- | ---: |
| `APROVADA` | 8 |
| `ATENDIDA` | 90 |
| `PENDENTE` | 18 |
| `REJEITADA` | 60 |

A massa inclui valores acima de R$ 500.000,00 com justificativa especial e respeita a regra de no maximo uma solicitacao aberta por agencia.

### Movimentacoes

540 movimentacoes identificadas pelo prefixo `seed-mass-v1-`:

| Tipo | Direcao | Quantidade |
| --- | --- | ---: |
| `ABASTECIMENTO` | Entrada | 90 |
| `DEPOSITO` | Entrada | 90 |
| `AJUSTE` | Saida | 90 |
| `RECOLHIMENTO` | Saida | 90 |
| `SAQUE` | Saida | 180 |

Cada agencia possui 18 movimentos distribuidos em tres ciclos historicos. Os saldos formam uma cadeia continua e o ultimo saldo coincide com `agencia.saldo_atual`.

Os abastecimentos estao ligados a solicitacoes atendidas.

## Aplicacao manual

Com o cliente MySQL no `PATH`:

```powershell
Get-Content .\database\scripts\seed-dados-dev.sql |
    mysql -u root -p gestao_numerario
```

Como o cliente local desta maquina nao esta no `PATH`, tambem pode ser usado:

```powershell
Get-Content .\database\scripts\seed-dados-dev.sql |
    & 'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe' -u root -p gestao_numerario
```

## Validacoes realizadas

O script foi aplicado duas vezes no MySQL local em 23/07/2026. Os totais permaneceram iguais na segunda execucao.

As consultas de verificacao retornaram zero para:

- Abastecimentos sem solicitacao vinculada.
- Solicitacoes acima de R$ 500.000,00 sem justificativa especial.
- Agencias com mais de uma solicitacao aberta.
- Quebras na cadeia de saldos das movimentacoes.
- Divergencia entre o ultimo saldo da cadeia e o saldo atual da agencia.

## Observacao sobre totais globais

Os totais acima consideram apenas os registros da massa V1. Registros locais anteriores, como as agencias `0001` a `0004`, podem continuar no banco; por isso, a contagem global das tabelas pode ser maior.

