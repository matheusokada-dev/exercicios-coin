# Modelo aprovado para evolução das solicitações de numerário

## Estado da decisão

Este documento registra o desenho aprovado na Etapa 2. O modelo ainda não foi
implementado. Nenhuma migration, classe, endpoint ou tela deve ser considerada
alterada por esta especificação.

As regras funcionais de origem estão em
`REGRAS_SOLICITACAO_NUMERARIO.md`.

## Decisões estruturais

- Somente `GESTOR` acessa e executa operações da Tesouraria.
- A solicitação admite `SUPRIMENTO` e `RECOLHIMENTO`.
- A tabela atual será transformada, preservando seus dados e identificadores.
- Origem e destino serão unidades operacionais.
- Cada solicitação admite uma única operação logística e uma única expedição.
- O valor aprovado deve ser igual ao valor solicitado.
- O valor efetivamente recebido será creditado no destino.
- Solicitação e operação logística possuem estados separados.
- Depois da aprovação não existe cancelamento.
- O histórico de eventos é completo, imutável e obrigatório.
- A API atual será mantida temporariamente e a evolução será publicada em
  `/api/v1`.

## Entidades

### Unidade operacional

Representa qualquer origem ou destino de numerário.

Tabela proposta: `unidade_operacional`.

| Campo | Tipo lógico | Regra |
| --- | --- | --- |
| `id` | identificador | chave primária |
| `tipo` | enum | `AGENCIA`, `TESOURARIA`, `BDN`, `CUSTODIANTE`, `PAB`, `OUTRO` |
| `codigo` | texto | obrigatório e único |
| `nome` | texto | obrigatório |
| `controla_saldo` | booleano | indica se a unidade possui posição operacional |
| `saldo_atual` | decimal | maior ou igual a zero, escala 2 |
| `ativo` | booleano | desativação lógica |
| `versao` | número | controle de concorrência |
| `criado_em` | instante UTC | obrigatório |
| `atualizado_em` | instante UTC | obrigatório |

Cada registro atual de `agencia` será associado a exatamente uma unidade
operacional do tipo `AGENCIA`. O saldo atual passa a pertencer à unidade
operacional. A agência continua armazenando limite mínimo, cidade e demais
atributos específicos.

A Tesouraria Central controla saldo e será criada com saldo zero. Sua carga
inicial deverá ocorrer por ajuste identificado e auditável.

A unidade `LEGADO-ORIGEM` não controla saldo e existe somente para documentar
a origem desconhecida de abastecimentos concluídos antes da evolução.

### Solicitação de numerário

A tabela `solicitacao_abastecimento` será renomeada e transformada em
`solicitacao_numerario`.

| Campo | Tipo lógico | Regra |
| --- | --- | --- |
| `id` | identificador | preserva os IDs atuais |
| `tipo_operacao` | enum | `SUPRIMENTO` ou `RECOLHIMENTO` |
| `agencia_referencia_id` | referência | agência escolhida pelo solicitante |
| `origem_id` | referência | unidade de origem; pode nascer vazia no suprimento |
| `destino_id` | referência | unidade de destino; pode nascer vazia no recolhimento |
| `valor_solicitado` | decimal | maior que zero, escala 2 |
| `motivo` | texto | obrigatório |
| `data_desejada` | data | não pode estar no passado na criação |
| `status` | enum | estado funcional da solicitação |
| `solicitante_id` | usuário | deve ser gestor |
| `aprovador_id` | usuário | pode ser igual ao solicitante |
| `justificativa_decisao` | texto | obrigatória para aprovação ou rejeição |
| `data_criacao` | instante UTC | obrigatório |
| `data_decisao` | instante UTC | preenchido na decisão |
| `cancelado_por_id` | usuário | preenchido somente no cancelamento pendente |
| `justificativa_cancelamento` | texto | obrigatória no cancelamento |
| `data_cancelamento` | instante UTC | preenchido no cancelamento |
| `versao` | número | bloqueio otimista |

Não haverá:

- valor aprovado diferente do solicitado;
- justificativa especial acima de R$ 500.000;
- composição por denominação;
- cancelamento depois da aprovação;
- mais de uma expedição por solicitação.

Regras de preenchimento:

- `SUPRIMENTO`: destino é a agência escolhida; origem é definida posteriormente.
- `RECOLHIMENTO`: origem é a agência escolhida; destino é definido posteriormente.
- Origem e destino devem estar ativos e ser diferentes.
- Antes da programação, origem e destino devem estar definidos.
- Uma origem operacional precisa controlar saldo para permitir expedição.

### Operação de numerário

Tabela proposta: `operacao_numerario`.

Existe no máximo uma operação para cada solicitação.

| Campo | Tipo lógico | Regra |
| --- | --- | --- |
| `id` | identificador | chave primária |
| `solicitacao_id` | referência | obrigatório e único |
| `origem_id` | referência | obrigatório |
| `destino_id` | referência | obrigatório |
| `status` | enum | estado logístico |
| `valor_programado` | decimal | igual ao valor solicitado |
| `valor_expedido` | decimal | preenchido na expedição |
| `valor_recebido` | decimal | preenchido no recebimento |
| `valor_divergencia` | decimal | expedido menos recebido |
| `programado_por_id` | usuário | gestor |
| `expedido_por_id` | usuário | gestor |
| `recebido_por_id` | usuário | gestor |
| `conciliado_por_id` | usuário | gestor, quando aplicável |
| `data_programacao` | instante UTC | obrigatório ao programar |
| `data_expedicao` | instante UTC | obrigatório ao expedir |
| `data_recebimento` | instante UTC | obrigatório ao receber |
| `data_conciliacao` | instante UTC | quando houver conciliação |
| `justificativa_divergencia` | texto | obrigatória se valores divergirem |
| `descricao_ocorrencia` | texto | obrigatória em ocorrência |
| `idempotency_key` | texto | única por comando financeiro |
| `versao` | número | bloqueio otimista |

### Histórico da solicitação

Tabela proposta: `historico_solicitacao_numerario`.

É append-only: não admite atualização ou exclusão comum.

| Campo | Tipo lógico | Regra |
| --- | --- | --- |
| `id` | identificador | chave primária |
| `solicitacao_id` | referência | obrigatório |
| `operacao_id` | referência | opcional |
| `evento` | enum | ação executada |
| `status_anterior` | texto | opcional na criação |
| `status_novo` | texto | obrigatório |
| `usuario_id` | usuário | obrigatório |
| `data_evento` | instante UTC | obrigatório |
| `justificativa` | texto | conforme o evento |
| `dados_complementares` | JSON | cópia dos valores relevantes do evento |

Eventos mínimos:

- `SOLICITACAO_CRIADA`
- `SOLICITACAO_APROVADA`
- `SOLICITACAO_REJEITADA`
- `SOLICITACAO_CANCELADA`
- `ORIGEM_DESTINO_DEFINIDOS`
- `OPERACAO_PROGRAMADA`
- `NUMERARIO_EXPEDIDO`
- `NUMERARIO_RECEBIDO`
- `DIVERGENCIA_REGISTRADA`
- `DIVERGENCIA_CONCILIADA`
- `OCORRENCIA_REGISTRADA`
- `SOLICITACAO_CONCLUIDA`

### Movimentação

A tabela `movimentacao` será preservada, passará a referenciar
`unidade_operacional` em vez de `agencia` e receberá referência opcional para
`operacao_numerario`.

Novos tipos:

- `SAIDA_PARA_TRANSITO`
- `ENTRADA_DE_TRANSITO`
- `AJUSTE_DIVERGENCIA`

Na expedição:

- cria `SAIDA_PARA_TRANSITO`;
- reduz o saldo da origem;
- registra saldo anterior e posterior;
- coloca o valor na operação logística em trânsito.

No recebimento:

- cria `ENTRADA_DE_TRANSITO`;
- credita somente o valor efetivamente recebido;
- registra saldo anterior e posterior.

Se houver diferença:

- a diferença permanece associada à operação;
- não é ajustada automaticamente;
- eventual ajuste é uma nova movimentação `AJUSTE_DIVERGENCIA`, com
  justificativa e usuário.

As movimentações antigas permanecem válidas e imutáveis.

## Máquinas de estado

### Solicitação

Fluxo principal:

```text
PENDENTE → APROVADA → EM_EXECUCAO → CONCLUIDA
```

Fluxos alternativos:

```text
PENDENTE → REJEITADA
PENDENTE → CANCELADA
EM_EXECUCAO → COM_DIVERGENCIA → CONCLUIDA
```

Regras:

- somente `PENDENTE` pode ser aprovada, rejeitada ou cancelada;
- aprovação exige justificativa;
- autoaprovação é permitida;
- cancelamento exige justificativa;
- `APROVADA` não pode ser cancelada;
- `EM_EXECUCAO` começa na programação da operação;
- conclusão exige recebimento sem divergência ou divergência conciliada.

### Operação logística

```text
PROGRAMADA → EM_SEPARACAO → EM_TRANSITO → RECEBIDA → CONCILIADA
```

Fluxos excepcionais:

```text
PROGRAMADA/EM_SEPARACAO/EM_TRANSITO → COM_OCORRENCIA
RECEBIDA → COM_DIVERGENCIA → CONCILIADA
```

Não existe cancelamento da operação logística, porque ela só é criada após a
aprovação. Uma impossibilidade de execução deve ser registrada como ocorrência
e resolvida por um evento posterior, sem apagar o fato aprovado.

## Invariantes transacionais

- Toda ação da Tesouraria exige perfil `GESTOR`.
- Aprovação não altera saldo.
- Programação não altera saldo.
- Expedição, movimento de saída e redução do saldo da origem ocorrem na mesma
  transação.
- Recebimento, movimento de entrada e aumento do saldo do destino ocorrem na
  mesma transação.
- A origem deve possuir saldo suficiente para expedir.
- O bloqueio para atualização financeira ocorre na unidade operacional.
- Unidade que não controla saldo não pode participar de nova expedição ou
  recebimento; `LEGADO-ORIGEM` é aceita somente nos registros migrados.
- Uma solicitação possui no máximo uma operação e uma expedição.
- Cada comando financeiro exige chave de idempotência.
- O valor expedido deve ser maior que zero e não pode ultrapassar o solicitado.
- Como não existe atendimento parcial, o valor expedido deve ser igual ao
  solicitado.
- O valor recebido deve ser maior que zero e não pode ultrapassar o expedido.
- Diferença entre expedido e recebido gera divergência obrigatoriamente.
- Uma movimentação persistida não pode ser editada ou excluída.
- Toda mudança de estado gera histórico na mesma transação.

## Contratos do fluxo evoluído na API v1

Base: `/api/v1/solicitacoes-numerario`.

| Método e rota | Finalidade |
| --- | --- |
| `POST /` | criar solicitação pendente |
| `GET /` | consultar com filtros e paginação |
| `GET /{id}` | consultar detalhes e histórico |
| `PUT /{id}/aprovar` | aprovar pelo valor solicitado |
| `PUT /{id}/rejeitar` | rejeitar com justificativa |
| `PUT /{id}/cancelar` | cancelar enquanto pendente |
| `PUT /{id}/definir-rota` | definir origem e destino |
| `PUT /{id}/programar` | criar a operação logística |
| `PUT /{id}/iniciar-separacao` | registrar início da preparação |
| `PUT /{id}/expedir` | debitar a origem e iniciar trânsito |
| `PUT /{id}/registrar-ocorrencia` | registrar impedimento logístico |
| `PUT /{id}/receber` | creditar o valor confirmado no destino |
| `PUT /{id}/conciliar` | encerrar divergência |

Consultas auxiliares:

| Método e rota | Finalidade |
| --- | --- |
| `GET /api/v1/unidades-operacionais` | selecionar origem ou destino |
| `GET /api/v1/operacoes-numerario` | consultar programação e trânsito |
| `GET /api/v1/solicitacoes-numerario/{id}/historico` | consultar auditoria |
| `POST /api/v1/tesouraria/carga-inicial` | realizar a carga inicial única da Tesouraria |
| `POST /api/v1/solicitacoes-numerario/{id}/ajustes-divergencia` | registrar ajuste financeiro separado |

Todos os endpoints exigem JWT e perfil `GESTOR`.

## Compatibilidade

- Os contratos existentes da API v1 continuam disponíveis.
- O fluxo evoluído também usa a API v1, em rotas específicas de numerário.
- Os registros atuais são convertidos para `SUPRIMENTO`.
- Solicitações antigas já finalizadas preservam seus estados e movimentações.
- Solicitações antigas abertas precisam de regra específica de migração na
  Etapa 3.
- O frontend usa o fluxo evoluído somente depois da API e do BFF estarem validados.
- Nenhuma retirada dos contratos existentes está prevista nesta migração.

## Decisões da Etapa 4 — backend

A implementação do backend será incremental, na ordem: domínio, consultas,
decisões, logística e financeiro.

- Os contratos existentes da API v1 permanecem disponíveis para consulta e
  alteração. O fluxo evoluído usa rotas adicionais sob o mesmo prefixo.
- A criação recebe tipo da operação, agência, valor, motivo e data desejada.
- Aprovação e definição da rota são ações distintas.
- A programação recebe a unidade ainda não definida, completa a rota e cria a
  operação em uma única transação.
- O estágio `EM_SEPARACAO` é opcional. Uma operação programada pode ser expedida
  diretamente.
- Programação, expedição, recebimento, conciliação e ajustes exigem o cabeçalho
  `Idempotency-Key`.
- Comandos de alteração exigem a versão atual do agregado. Conflitos de
  concorrência retornam HTTP `409`.
- O recebimento aceita valor maior que zero e menor ou igual ao expedido. Valor
  menor exige justificativa e abre divergência.
- A conciliação da divergência é documental. Qualquer ajuste financeiro é
  registrado separadamente.
- Uma ocorrência é registrada como evento sem substituir o estágio logístico
  atual. Depois do registro, a operação pode continuar normalmente.
- A carga inicial da Tesouraria usa endpoint exclusivo, permitido uma única vez,
  com valor, justificativa e chave de idempotência.
- O ajuste de divergência informa unidade da rota, direção, valor, justificativa
  e versão da unidade. O valor não pode superar a divergência registrada e a
  movimentação fica vinculada à operação.
- Gestores podem consultar todas as unidades. Unidades inativas aparecem em
  históricos, mas não nos seletores de novas operações.
- O detalhe da solicitação agrega solicitação, rota, operação e histórico.
- Todos os endpoints do fluxo evoluído, inclusive consultas, exigem perfil `GESTOR`.

Como a ocorrência não altera o estágio logístico, `COM_OCORRENCIA` deixa de ser
um estado da máquina de operação para novos registros. O fato permanece
preservado no histórico imutável e em sua descrição.

## Pontos para a Etapa 3

A Etapa 3 deverá detalhar e submeter à aprovação:

- migration incremental e reversível por nova migration;
- transformação segura da tabela atual;
- criação das unidades operacionais para as agências existentes;
- tratamento das solicitações legadas abertas;
- constraints, índices e chaves estrangeiras;
- compatibilidade dos seeds;
- plano de validação antes e depois da migration.
