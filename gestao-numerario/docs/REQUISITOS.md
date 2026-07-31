# Requisitos rastreáveis

Legenda: `Pendente`, `Em andamento`, `Concluído`. Origem: **Gestor** ou **Guia**.

## Funcionalidades

| ID | Requisito | Origem | Status |
| --- | --- | --- | --- |
| RF01 | Login com campos obrigatórios, JWT, perfis Operador/Gestor e bloqueio temporário após cinco falhas | Gestor | Implementado |
| RF02 | Dashboard: numerário total da rede, agências em alerta, solicitações pendentes e abastecimentos do dia | Gestor | Em andamento |
| RF03 | Consulta de agências com pesquisa, ordenação, paginação e status OK/Alerta | Gestor | Em andamento |
| RF04 | Detalhe da agência com saldo, entradas e saídas do dia, saldo previsto e histórico | Gestor | Em andamento |
| RF05 | Solicitação de abastecimento com agência, valor, motivo e data desejada | Gestor | Em andamento |
| RF06 | Aprovação ou rejeição de solicitações por gestor | Gestor | Em andamento |
| RF07 | Histórico de movimentações com filtros por período, agência e tipo | Gestor | Em andamento |
| RF08 | CRUD completo de agências: código, nome, cidade, saldo atual e limite mínimo | Gestor | Em andamento |
| RF09 | Frontend Angular separado em `frontend-numerario` | Usuário | Em andamento |
| RF10 | BFF Spring MVC separado em `bff-numerario` | Usuário | Concluído |
| RF11 | API Spring em arquitetura hexagonal em `api-numerario` | Usuário | Em andamento |
| RF12 | Banco MySQL com migrations Flyway | Usuário/Guia | Em andamento |
| RF13 | Menu COIN responsivo para acesso às operações disponíveis ao perfil | Usuário | Concluído |
| RF14 | Loading global; toast de sucesso apenas para operações de alteração e toast de erro para falhas HTTP | Usuário | Concluído |
| RF15 | Página de erro para URL inválida, acesso indevido e indisponibilidade da API/BFF | Usuário | Concluído |
| RF16 | Camada frontend de mapeamento dos perfis COIN0001 a COIN0006, com compatibilidade temporária para GESTOR/OPERADOR | Usuário | Concluído |
| RF17 | COIN Home com Tesouraria e Cadastros; Tesouraria possui menu próprio e Cadastros abre erro de não implementado | Usuário | Concluído |
| RF18 | Consulta de pontos com autocomplete, filtros combinados, seleção e paginação de 50 | Usuário | Pendente |
| RF19 | Upload XLSX com resumo/log e relatórios Excel | Usuário | Pendente |
| RF20 | Livro Caixa por agência e período com download de arquivo Excel | Usuário | Concluído |
| RF21 | Header autenticado somente com logout; breadcrumbs e Voltar nas telas internas | Usuário | Concluído |
| RF22 | Padronizar o frontend conforme a documentação Bradesco/Liquid fornecida, distinguindo regra oficial, adaptação Angular e componente ainda sem referência | Usuário | Em andamento |
| RF23 | Documentar todos os controllers e operações da API e do BFF no padrão `*Api` → `Controller implements`, validando o OpenAPI automaticamente | Usuário | Concluído |

## Regras de negócio

| ID | Regra | Proteção esperada | Status |
| --- | --- | --- | --- |
| RN01 | Valor solicitado é maior que zero | Bean Validation, domínio e banco | Em andamento |
| RN02 | Motivo é obrigatório e tem conteúdo útil | Validação de entrada e domínio | Em andamento |
| RN03 | Data desejada não pode estar no passado | Validação de entrada e domínio | Em andamento |
| RN04 | Não existe mais de uma solicitação aberta (PENDENTE/APROVADA) por agência | Serviço transacional e banco | Em andamento |
| RN05 | Apenas GESTOR aprova ou rejeita | Autorização | Em andamento |
| RN06 | Solicitante não aprova a própria solicitação | Domínio | Em andamento |
| RN07 | Acima de R$ 500.000 exige justificativa especial | Domínio | Em andamento |
| RN08 | Apenas PENDENTE pode ser aprovada ou rejeitada | Máquina de estados | Em andamento |
| RN09 | Apenas APROVADA pode ser atendida | Máquina de estados | Em andamento |
| RN10 | Atendimento cria ABASTECIMENTO e aumenta saldo na mesma transação | Serviço transacional | Em andamento |
| RN11 | RECOLHIMENTO e SAQUE não podem deixar saldo negativo | Domínio sob bloqueio da agência | Concluído |
| RN12 | Saldo abaixo do mínimo sugere abastecimento igual à diferença | Caso de uso/dashboard | Concluído |
| RN13 | Valores monetários usam BigDecimal, escala 2 e arredondamento explícito | Domínio e persistência | Concluído |
| RN14 | Movimentações são imutáveis; não há update/delete comum | API e domínio | Concluído |
| RN15 | Agência presente no histórico não é apagada fisicamente; usar `ativo` | API e persistência | Em andamento |

## Pendências de validação

- Ampliar os testes automatizados dos novos componentes e interceptores globais.
- Atualizar status para `Concluído` somente após validação funcional ou execução de testes, conforme decisão do usuário.

## Evolução aprovada de solicitações de numerário

As regras-alvo aprovadas em 24/07/2026 estão formalizadas em
`REGRAS_SOLICITACAO_NUMERARIO.md`. Elas ainda não estão implementadas.

Essa evolução:

- restringe toda a Tesouraria ao perfil `GESTOR`;
- generaliza a solicitação para `SUPRIMENTO` e `RECOLHIMENTO`;
- permite que qualquer gestor solicite para qualquer agência;
- permite aprovação por qualquer gestor, inclusive autoaprovação;
- remove a justificativa especial acima de R$ 500.000;
- introduz origem, destino e numerário em trânsito;
- separa aprovação, programação, expedição, recebimento e conclusão;
- adiciona cancelamento, ocorrência, divergência e conciliação;
- mantém somente os perfis `GESTOR` e `OPERADOR`;
- exige controles compensatórios e histórico imutável.

As regras legadas RN04, RN06, RN07, RN09 e RN10 deverão ser substituídas
durante a implementação. Até lá, continuam descrevendo o comportamento do
código atual.

O desenho técnico aprovado para essa evolução está em
`MODELO_EVOLUCAO_SOLICITACAO_NUMERARIO.md`. Ele define unidade operacional,
transformação da tabela atual, operação logística única, histórico completo,
movimentações de trânsito, máquinas de estado separadas e contratos evoluídos da API v1.

## Regra de atualização

Ao implementar uma funcionalidade ou regra, atualizar seu status e apontar a migration, classe, endpoint ou tela correspondente. Nenhum requisito novo será inferido sem confirmação.
