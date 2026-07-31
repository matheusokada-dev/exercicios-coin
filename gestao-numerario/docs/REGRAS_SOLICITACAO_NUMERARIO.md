# Regras aprovadas para solicitações de numerário

## Estado da decisão

As regras deste documento foram aprovadas pelo usuário em 24/07/2026.
Elas definem a evolução desejada do produto, mas ainda não estão implementadas
no banco, na API, no BFF ou no frontend.

Até a conclusão das etapas técnicas, o sistema continua operando com o modelo
legado de solicitação de abastecimento.

## Escopo da Tesouraria

- Somente usuários com perfil `GESTOR` podem acessar a Tesouraria.
- Usuários com perfil `OPERADOR` não podem acessar dashboard, agências,
  solicitações, movimentações ou Livro Caixa da Tesouraria.
- Permanecem apenas os perfis atuais `GESTOR` e `OPERADOR`.
- Qualquer gestor pode atuar sobre qualquer agência; não haverá vínculo ou
  restrição por agência, regional ou unidade.

## Tipos de solicitação

A solicitação passa a representar uma operação de numerário e admite:

- `SUPRIMENTO`: entrada de numerário em uma agência.
- `RECOLHIMENTO`: retirada de numerário de uma agência.

Transferência direta entre duas agências não faz parte desta evolução.

## Solicitante, origem e destino

- Somente gestores podem criar solicitações.
- Qualquer gestor pode solicitar para qualquer agência.
- No suprimento, o solicitante escolhe a agência de destino e a Tesouraria
  define posteriormente a origem.
- No recolhimento, o solicitante escolhe a agência de origem e a Tesouraria
  define posteriormente o destino.
- O solicitante informa somente o valor total; composição por denominação não
  será exigida.

## Aprovação

- Toda solicitação de suprimento ou recolhimento precisa ser aprovada.
- Qualquer gestor pode aprovar ou rejeitar qualquer solicitação.
- Não haverá alçada diferenciada por valor.
- A autoaprovação é permitida.
- A justificativa da decisão permanece obrigatória.
- A justificativa especial para valores acima de R$ 500.000 deixa de existir.
- O histórico deve identificar separadamente solicitante e aprovador, inclusive
  quando forem a mesma pessoa.

## Saldo e numerário em trânsito

A aprovação não altera o saldo.

### Suprimento

1. A solicitação é aprovada.
2. A origem é definida.
3. A expedição reduz o saldo da origem.
4. O valor passa a ser registrado como numerário em trânsito.
5. O recebimento aumenta o saldo do destino.

### Recolhimento

1. A solicitação é aprovada.
2. O destino é definido.
3. A expedição ou coleta reduz o saldo da agência de origem.
4. O valor passa a ser registrado como numerário em trânsito.
5. O recebimento aumenta o saldo do destino.

## Recebimento e divergência

- Qualquer usuário autenticado pode confirmar o recebimento.
- Como a Tesouraria é exclusiva para gestores, na interface da Tesouraria
  qualquer gestor poderá executar a confirmação.
- Não será exigido vínculo entre o recebedor e a unidade destinatária.
- A confirmação deve ser explícita e registrar usuário, data e hora.
- Quando o valor recebido for diferente do valor expedido, o sistema registra o
  valor efetivamente recebido e abre uma divergência.
- A divergência exige justificativa e não pode ser ajustada automaticamente.
- Devem ser preservados os valores solicitado, expedido, recebido e a diferença.
- A operação com divergência permanece pendente de conciliação.

## Cancelamento

- Antes da aprovação, a solicitação pode ser cancelada.
- Depois da aprovação e antes da expedição, o cancelamento exige confirmação.
- Depois da expedição, a solicitação não pode ser cancelada.
- Problemas posteriores à expedição são tratados como ocorrência logística,
  divergência ou devolução.

## Estados aprovados

Fluxo principal:

```text
PENDENTE
→ APROVADA
→ PROGRAMADA
→ EM_TRANSITO
→ RECEBIDA
→ CONCLUIDA
```

Fluxos alternativos:

```text
PENDENTE → REJEITADA
PENDENTE/APROVADA/PROGRAMADA → CANCELADA
EM_TRANSITO → COM_OCORRENCIA
RECEBIDA → COM_DIVERGENCIA → CONCLUIDA
```

## Controles obrigatórios

Como qualquer gestor pode solicitar, autoaprovar e confirmar uma operação, os
seguintes controles compensatórios são obrigatórios:

- histórico imutável de ações e transições;
- usuário, data e hora em cada ação;
- motivo obrigatório na solicitação;
- justificativa obrigatória na aprovação, rejeição, cancelamento e divergência;
- confirmação explícita antes de expedir e receber;
- idempotência para impedir duplicidade;
- movimentações sem edição ou exclusão comum;
- saldo anterior e posterior em cada movimentação;
- Livro Caixa com origem, destino e solicitação relacionada;
- identificação de autoaprovações para consulta e auditoria;
- consulta específica de solicitações com divergência;
- autorização validada pela API, independentemente do frontend.

## Impacto sobre regras legadas

Quando esta evolução for implementada:

- `SolicitacaoAbastecimento` será substituída ou generalizada;
- a proibição de autoaprovação será removida;
- a justificativa especial acima de R$ 500.000 será removida;
- o atendimento imediato será substituído por programação, expedição,
  recebimento e conclusão;
- o saldo deixará de ser atualizado no simples atendimento;
- operadores perderão o acesso às operações da Tesouraria.
