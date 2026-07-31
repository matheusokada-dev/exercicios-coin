# Diário de implementação

## 30/07/2026 - OpenAPI completo e inicialização validada

- Centralizada a documentação funcional nas interfaces `*Api`; todos os
  controllers da API e do BFF implementam seu respectivo contrato.
- As 34 operações da API e as 32 operações do BFF passaram a possuir resumo,
  descrição e `operationId` no JSON OpenAPI.
- API e BFF publicam o esquema `bearerAuth`; o login foi explicitamente
  documentado como público.
- Respostas `400`, `401`, `403` e `500` foram padronizadas. O BFF também
  documenta `503` e `504` para dependências indisponíveis ou com timeout.
- Criado `scripts/validar-openapi.mjs`, que audita as 66 operações diretamente
  nos endpoints `/v3/api-docs`.
- `scripts/iniciar-tudo.ps1` passou a validar `.env`, Maven Wrapper, Angular
  local e disponibilidade das quatro portas antes da inicialização.
- O inicializador agora exibe os links do frontend, Swagger da API, Swagger do
  BFF, serviço de relatórios e comando de validação.

## 29/07/2026 - Geração centralizada do Livro Caixa

- Removida a geração de Excel do navegador e mantido o frontend sem biblioteca
  JavaScript de planilhas.
- O BFF passou a montar colunas, linhas, totalizadores e metadados do Livro
  Caixa e chamar `POST /v1/relatorios/gerar`.
- Criado o microsserviço Java `relatorio-numerario`, na porta `8082`, compatível com
  o contrato documentado e responsável por gerar `.xlsx` com Apache POI.
- O script `scripts/iniciar-tudo.ps1` passou a iniciar API, serviço de
  relatórios, BFF e frontend.
- O download do Angular passou a manter a URL temporária ativa até o navegador
  iniciar o recebimento do arquivo.
- Validações concluídas: arquivo XLSX real com assinatura `PK`, testes do novo
  serviço, 39 testes do BFF, TypeScript estrito e build de produção Angular.

## 24/07/2026 - Referência Bradesco/Liquid para o frontend

- Analisado o material fornecido sobre Pagination, Chart Donut, Alert, Modal Dialog e Typography.
- Criado `docs/PADRAO_FRONTEND_BRADESCO.md` como referência versionada de UI.
- Registrados família tipográfica, escala, pesos, alturas de linha, cores estendidas de gráfico, contratos dos componentes e regras de acessibilidade.
- Separadas regras extraídas da fonte, adaptações necessárias ao Angular e decisões provisórias do projeto.
- Card, paleta institucional primária, espaçamento, botões, campos, tabelas e navegação permanecem pendentes de documentação oficial.
- Registrado RF22 para acompanhar a migração progressiva do frontend ao padrão.

## 24/07/2026 - Padronização monetária e feedback de consultas

- Registrado o locale global `pt-BR` no frontend.
- Exibições monetárias do dashboard, agências, detalhe da agência, solicitações e movimentações passaram a usar `CurrencyPipe` com moeda `BRL`.
- O contrato do dashboard no Angular passou a ser tipado, removendo o uso de `any` nessa resposta.
- Requisições `GET` deixaram de gerar toast de sucesso; o loading global e os toasts de erro foram preservados.
- `dataReferencia` continua presente no contrato do dashboard sem obrigatoriedade de exibição na tela.

## 24/07/2026 - Evolução visual do dashboard

- Dashboard reestruturado como painel operacional, com hierarquia visual, data de referência, atualização manual, indicadores contextualizados, prioridades e atalhos.
- Mantidos somente indicadores derivados do contrato existente, sem criar métricas ou limiares não confirmados pelo negócio.
- Atalhos de agências, solicitações e movimentações abrem as telas com o filtro correspondente.
- Ações restritas a gestor são ocultadas para os demais perfis.
- Criados estados próprios de carregamento e erro, além de comportamento responsivo para desktop, tablet e celular.
- Orçamento de estilo por componente ajustado para comportar o painel isolado sem alterar o orçamento do bundle inicial.

## 24/07/2026 - Validação e proteção do login

- Template HTML removido de `LoginComponent` e criado `login.component.html`.
- Formulário migrado para Reactive Forms, com mensagens específicas para login e senha obrigatórios e sem chamada ao BFF quando inválido.
- Criada a migration V3 com `tentativas_login_falhas` e `bloqueado_ate` na tabela `usuario`.
- Definido bloqueio persistente de 15 minutos após cinco senhas incorretas consecutivas.
- Uma autenticação válida limpa tentativas anteriores.
- Resposta `401` permanece igual para usuário inexistente, inativo, bloqueado ou senha incorreta.
- Adicionados testes unitários do caso de uso para bloqueio, limpeza após sucesso e rejeição durante o bloqueio.
- A tela passou a avisar previamente sobre o limite, informar as tentativas restantes e mostrar o horário de desbloqueio.
- A marca do login foi ajustada para `COIN Numerário`, com menor espaçamento entre as palavras.

## 23/07/2026 - Documentação consolidada em HTML

- Criado `docs/DOCUMENTACAO_COMPLETA.html`.
- Consolidados escopo, conceitos de negócio, regras, fluxos, arquitetura, estrutura, tecnologias, dados, segurança e escolhas técnicas.
- Separados visualmente os estados implementado, em andamento e pendente.
- Registrado que Cliente, Ponto, Cofre Inteligente, PAB, BDN e frequências cíclicas ainda não fazem parte do modelo atual.

## 23/07/2026 - COIN Home e mapeamento de perfis

- Criada camada de perfis `COIN0001` a `COIN0006` no frontend.
- Compatibilidade temporária: `GESTOR -> COIN0001` e `OPERADOR -> COIN0003`.
- `/menu` passou a ser COIN Home.
- A raiz `/` redireciona para `/login`; após autenticação o fluxo segue para `/menu`.
- Tesouraria direciona para `/tesouraria`, com cards para as cinco operações definidas.
- Cadastros direciona para `/erro?tipo=cadastros`, pois o módulo ainda não foi desenvolvido.
- Removido o menu horizontal do header; permaneceu somente logout.
- Adicionados breadcrumbs e botão Voltar às telas internas.
- Implementado Livro Caixa por agência e período com geração XLSX no navegador.
- Livro Caixa pagina os endpoints existentes de agências e movimentações e é protegido por `gestorGuard`.
- Build Angular concluído; ExcelJS ficou isolado no chunk lazy da rota Livro Caixa.
- Validado no Chrome: login na raiz, proteção sem token, menus desktop/mobile, erro de Cadastros, breadcrumbs e XLSX válido.
- Consulta de pontos, importação, histórico e relatórios não foram implementados.

## 21/07/2026 - Reestruturacao MVC do BFF

- Reorganizado o BFF nas camadas `controller`, `service`, `client`, `dto`, `exception` e `config`.
- Substituidos payloads `JsonNode` por contratos tipados e validados.
- Separadas as rotas de aprovar, rejeitar e atender solicitacoes.
- Preservados status e payloads de erro recebidos da API.
- Configurados timeouts de conexao e leitura para a integracao com a API.
- Adicionados testes de arquitetura, controllers, client e tratamento de erros.

## 20/07/2026 - Ambiente, API e banco

- Confirmados Java 21, Node 22, npm 10, Angular CLI 19 e Git.
- Criados os projetos `frontend-numerario`, `bff-numerario` e `api-numerario`.
- API criada com Spring Boot 3.5.14, Maven, Java 21 e pacote-base `br.com.gestaonumerario.api`.
- Estrutura hexagonal consolidada em `adapter`, `core`, `port` e `config`.
- Banco MySQL local `gestao_numerario` confirmado.
- Profile `local` criado; credenciais locais ficam em `application-local.properties`, ignorado pelo Git.
- Flyway ficou responsável por evoluir o schema; Hibernate usa `ddl-auto=validate`.

## Decisões registradas

- D01: uma solicitação aberta por agência é garantida no MySQL por coluna gerada armazenada e índice único, além da validação transacional no domínio.
- D02: os contratos de solicitação têm uma única implementação inicial, `SolicitacaoService`, preservando contratos de entrada separados.
- D03: operações compostas usam `TransacaoPort`, mantendo o core sem dependência de `@Transactional`.

## 20/07/2026 - Núcleo de domínio e persistência

- Criados enums de perfil, status de solicitação e tipo de movimentação.
- Criado padrão de erros com `ErrorEnum`, `BaseException`, exceções concretas e handler REST.
- Criados modelos de domínio `Usuario`, `Agencia`, `SolicitacaoAbastecimento`, `Movimentacao` e `ValorMonetario`.
- Criados ports de entrada, commands e ports de saída.
- Criadas entidades JPA, repositories, mappers e adapters de persistência.
- Criado `SpringTransacaoAdapter`.
- Migrations V1 e V2 existem em `api-numerario/src/main/resources/db/migration`.

## 21/07/2026 - API REST

- Criados endpoints de solicitações: criar, listar, aprovar, rejeitar e atender.
- Criados endpoints de agências: criar, listar, consultar, detalhar, atualizar e desativar.
- Criados endpoints de usuários: criar e consultar.
- Criado endpoint de movimentações manuais e histórico paginado.
- Criado endpoint de dashboard operacional.
- Criado login com JWT Bearer de acesso, válido por 60 minutos, sem refresh token.
- Rotas passaram a exigir autenticação; agências/usuários e decisão de solicitações requerem `GESTOR`.
- IDs de usuário foram removidos dos corpos de criação/decisão de solicitação e movimentação quando aplicável; agora vêm do token.

## 21/07/2026 - BFF MVC

- Criado `bff-numerario`, Spring MVC na porta 8080.
- BFF configurado para consumir a API na porta 8081.
- Criado login no BFF em `POST /api/v1/auth/login`.
- Criadas rotas BFF para dashboard, agências, solicitações e movimentações, encaminhando o JWT Bearer para a API.

## 21/07/2026 - Frontend Angular

- Criado Angular standalone com rotas protegidas.
- Criado `AuthService`, `authGuard` e interceptor JWT.
- Frontend consome o BFF por rotas relativas `/api/v1`.
- Criadas telas iniciais de dashboard, agências, detalhe de agência, solicitações e movimentações.
- Criada tela de login em `/login`.

## 21/07/2026 - Inicialização local

- Criado `scripts/iniciar-tudo.ps1`, que inicia API (8081), BFF (8080) e frontend (4200) em terminais separados.
- O primeiro gestor foi criado diretamente no MySQL, conforme decisão do usuário.

## 21/07/2026 - Atualização de documentação e login

- Documentação principal atualizada para refletir o estado real do projeto.
- Contratos atualizados para registrar o fluxo autenticado por JWT Bearer.
- Tela de login Angular revisada com texto legível, formulário estruturado, estado de carregamento e mensagem de erro.
- Layout principal teve textos com encoding quebrado corrigidos.
- Rota raiz do frontend passou a redirecionar para dashboard dentro do layout autenticado.
- Corrigidos imports ausentes no controller BFF de agências.
- Testes e compilações automatizadas não foram executados por decisão do usuário.

## 21/07/2026 - Seed local para uso do frontend

- Criado `database/scripts/seed-dados-dev.sql` para popular agências, solicitações e movimentações de exemplo no MySQL local.
- O seed não cria nem altera senhas de usuários; ele reutiliza o primeiro usuário ativo com perfil `GESTOR`.
- A carga usa códigos e chaves de idempotência fixas para poder ser reaplicada sem duplicar dados.
- O script fica fora das migrations Flyway para não levar dados fictícios para ambientes futuros.

## Próximo registro esperado

Validação manual do fluxo autenticado pelo frontend, usando o gestor já criado.

## 21/07/2026 - Credencial do gestor local

- Criado `database/scripts/upsert-gestor-dev.sql` para criar ou atualizar o login local `gestor`.
- A senha de desenvolvimento foi definida como `admin123`, armazenada no banco somente como BCrypt.
- O script reativa o usuário e garante o perfil `GESTOR` quando o login já existe.
- A credencial é exclusiva para desenvolvimento local.
- O script do gestor e o seed foram aplicados no MySQL local: 4 agências, 6 solicitações e 4 movimentações disponíveis.
- A tela de login passou a diferenciar credenciais inválidas de API/BFF indisponíveis.
- Corrigida a configuração local da API com `app.security.jwt.secret`, cuja ausência impedia a criação do serviço JWT e a inicialização na porta 8081.
- Testes e compilações não foram executados, conforme solicitado.

## 23/07/2026 - Análise inicial das stories do COINCAD

- Analisadas as stories de autenticação GIDE, consulta de pontos, histórico de clientes, importação massiva e relatório consolidado.
- Confirmado que a arquitetura pode receber o novo domínio, mas o modelo funcional atual ainda não atende ao COINCAD.
- Registrados escopo, dependências, contradições e ordem recomendada em `ANALISE_STORIES_COINCAD.md`.
- Nenhuma proposta técnica foi promovida a decisão confirmada e nenhum código foi alterado nesta etapa.
- Confirmado que a primeira expansão da massa de desenvolvimento usará apenas as tabelas já implementadas, sem Cliente, Ponto, Cofre ou Frequência.

## 23/07/2026 - Ampliação da massa de desenvolvimento

- `database/scripts/seed-dados-dev.sql` foi ampliado para gerar dados determinísticos das quatro tabelas existentes.
- A massa identificada possui 7 usuários, 30 agências, 176 solicitações e 540 movimentações.
- Foram incluídos usuários ativos e inativo, agências ativas/inativas, saldos abaixo/acima/no limite e solicitações em todos os status.
- As movimentações cobrem abastecimento, depósito, ajuste, recolhimento e saque em três ciclos históricos por agência.
- O script foi aplicado duas vezes no MySQL local sem duplicar os registros da massa.
- Foram validados vínculos de abastecimento, justificativas especiais, unicidade de solicitação aberta, continuidade dos saldos e saldo final das agências; todas as verificações retornaram zero inconsistências.
- A composição e o procedimento de reaplicação estão documentados em `MASSA_DADOS.md`.

## 23/07/2026 - Base global de experiência do frontend

- Criado Menu COIN responsivo com acessos para visão geral, agências, solicitações e movimentações.
- Adicionada a biblioteca `lucide-angular` para os ícones do menu e dos controles.
- Criados `LoadingService`, `NotificationService` e interceptor global de feedback HTTP.
- Criados componentes globais de loading e toast.
- Criado `gestorGuard`; rotas de agências agora bloqueiam perfis sem permissão antes de abrir a tela.
- Criada página central de erro para URL inválida, acesso indevido e indisponibilidade da API/BFF.
- O build de produção foi concluído com sucesso.
- A validação no Chrome cobriu desktop e mobile, loading com requisição atrasada, toast de sucesso, toast de erro, acesso 403, URL 404 e falha simulada da API/BFF.

## 24/07/2026 - Regras aprovadas para evolução das solicitações

- Concluída a Etapa 1 de definição funcional, sem alteração de código ou banco.
- A Tesouraria será acessível somente pelo perfil `GESTOR`.
- A solicitação será generalizada para `SUPRIMENTO` e `RECOLHIMENTO`.
- Qualquer gestor poderá solicitar para qualquer agência, aprovar, autoaprovar
  e confirmar recebimento.
- Não haverá alçada diferenciada por valor.
- A justificativa especial acima de R$ 500.000 será removida.
- A Tesouraria definirá a origem no suprimento e o destino no recolhimento.
- A composição por denominação não fará parte desta evolução.
- O saldo será atualizado por expedição e recebimento, com numerário em trânsito.
- Foram aprovados cancelamento, ocorrência, divergência e conciliação.
- As decisões e os controles compensatórios estão formalizados em
  `REGRAS_SOLICITACAO_NUMERARIO.md`.
- As regras foram marcadas como aprovadas e ainda não implementadas.

## 24/07/2026 - Modelo aprovado para evolução das solicitações

- Concluída a Etapa 2 de desenho técnico, sem migration ou alteração do código.
- A tabela atual de solicitação será transformada e manterá os IDs existentes.
- Foi aprovada a entidade `unidade_operacional` para representar origem e
  destino.
- Cada solicitação terá uma única `operacao_numerario` e uma única expedição.
- O valor aprovado será sempre igual ao valor solicitado.
- Solicitação e operação logística terão máquinas de estado separadas.
- Expedição gerará saída para trânsito e recebimento gerará entrada de trânsito.
- O destino será creditado pelo valor efetivamente recebido.
- Divergências serão registradas e conciliadas sem ajuste automático.
- Somente gestores poderão confirmar o recebimento.
- Depois da aprovação não haverá cancelamento.
- Enquanto pendente, solicitante ou qualquer gestor poderá cancelar com
  justificativa.
- Foi aprovado histórico completo e imutável de eventos.
- A evolução será exposta em `/api/v1`, mantendo a v1 temporariamente.
- O desenho está formalizado em
  `MODELO_EVOLUCAO_SOLICITACAO_NUMERARIO.md`.

## 24/07/2026 - Rascunho da migration de solicitações

- Aprovado que o saldo passe a pertencer a `unidade_operacional`.
- Agências e Tesouraria Central controlarão saldo.
- A unidade `LEGADO-ORIGEM` será apenas documental e não controlará saldo.
- Tesouraria Central será criada com saldo zero e exigirá ajuste inicial
  auditável antes de expedir novos suprimentos.
- Definida a migração das solicitações pendentes, aprovadas, rejeitadas e
  concluídas, preservando IDs e dados históricos.
- Solicitações concluídas serão relacionadas a operações sintéticas com origem
  histórica não informada.
- Eventos históricos sintéticos preservarão inclusive a justificativa especial
  legada antes da remoção da coluna.
- O plano exige parada controlada e backup obrigatório.
- Criado o rascunho
  `database/design/V4__evolucao_solicitacao_numerario_DRAFT.sql`.
- O arquivo está fora da pasta do Flyway e não foi executado.

## 24/07/2026 - Validação isolada do rascunho V4

- O banco `gestao_numerario` foi clonado para uma base temporária, sem alteração
  do banco original.
- O primeiro ensaio identificou dependências de índices ainda utilizados por
  chaves estrangeiras; a ordem das operações foi corrigida no rascunho.
- A base temporária foi recriada a partir do original e a V4 foi executada
  integralmente, sem erros.
- As seis consultas de inconsistência retornaram zero.
- Foram preservadas 176 solicitações e 540 movimentações, com 90 operações
  históricas e 424 eventos sintéticos.
- Os status foram preservados: 18 pendentes, 8 aprovadas, 60 rejeitadas e
  90 atendidas convertidas para concluídas.
- O saldo agregado das agências permaneceu em R$ 11.260.000,00.
- A Tesouraria Central iniciou com saldo zero e `LEGADO-ORIGEM` permaneceu sem
  controle de saldo e com saldo zero.
- Esta validação comprova a compatibilidade estrutural do rascunho com a massa
  local; ainda não autoriza promover o arquivo para o Flyway nem aplicá-lo no
  banco principal.

## 24/07/2026 - Decisões do backend evoluído

- Aprovada a implementação incremental do backend em cinco blocos: domínio,
  consultas, decisões, logística e financeiro.
- A v1 será mantida temporariamente somente para consultas.
- Criação, aprovação e programação permanecem ações auditáveis distintas; a
  programação completa a rota e cria a operação na mesma transação.
- Separação será opcional.
- Comandos financeiros exigirão `Idempotency-Key` e comandos de alteração
  exigirão versão para controle de concorrência com resposta HTTP `409`.
- Recebimento acima do expedido não será permitido.
- Divergência terá conciliação documental e ajuste financeiro separado.
- Ocorrência será um evento que não substitui o estágio logístico.
- A carga inicial da Tesouraria terá endpoint exclusivo e execução única.
- Consultas e operações do fluxo evoluído serão exclusivas de gestores.
- O detalhe da solicitação será agregado, incluindo operação e histórico.
- As decisões foram registradas sem alteração de código ou banco.

## 24/07/2026 - Bloco 4.1: domínio evoluído da API v1

- Criado o domínio evoluído em paralelo ao legado, sem alterar controllers,
  persistência, endpoints ou o banco principal.
- Implementados os agregados `SolicitacaoNumerario`, `OperacaoNumerario` e
  `UnidadeOperacional`, além do histórico imutável.
- Implementados os novos tipos, estados e eventos do fluxo.
- Criação e todas as ações da Tesouraria validam o perfil `GESTOR`.
- Autoaprovação é permitida e não existe justificativa especial por valor.
- Aprovação, rejeição, cancelamento, programação, separação opcional, expedição,
  recebimento, divergência, conciliação e ocorrência possuem transições
  protegidas.
- Programação completa a rota; expedição debita a origem; recebimento credita
  somente o valor efetivamente recebido.
- Ocorrências não substituem o estágio logístico e permanecem registradas como
  eventos.
- Agregados e unidades possuem validação de versão com conflito HTTP lógico
  `409`.
- Foram adicionadas fábricas de reconstituição que não geram eventos novos ao
  carregar dados persistidos.
- O rascunho V4 foi alinhado, removendo `COM_OCORRENCIA` da constraint de estado
  da operação.
- A suíte foi executada com sucesso: 18 testes, nenhuma falha e nenhum erro,
  sendo 14 testes novos do domínio evoluído.
- A V4 continua fora do Flyway e não foi executada no banco principal.

## 24/07/2026 - Bloco 4.2: persistência e consultas

- Criadas entidades JPA para unidade operacional, operação e histórico; as
  entidades legadas foram adaptadas ao esquema V4.
- Criados repositórios para unidades, operações e histórico, com bloqueio
  pessimista da unidade para futuras transações financeiras.
- Implementados filtros paginados de solicitação e operação por tipo, status,
  agência, origem, destino e período.
- Implementado detalhe agregado com solicitação, rota, operação e histórico.
- Seletores retornam somente unidades ativas; registros inativos continuam
  acessíveis por identificador e histórico.
- Implementada verificação global de idempotência nas operações e movimentações.
- Implementada persistência transacional de solicitação, operação, unidade e
  eventos novos.
- O controle de concorrência ficou sob responsabilidade exclusiva de
  `@Version`; o domínio valida a versão recebida e o JPA realiza o incremento.
- Usuários operadores encontrados em eventos históricos podem ser
  reconstituídos, mas comandos novos continuam exclusivos de gestores.
- Consultas v1 foram adaptadas ao esquema V4. Todas as mutações v1, exceto
  autenticação, passam a responder HTTP `410 Gone`.
- O contexto Hibernate validou as oito tabelas da V4 em uma cópia temporária.
- A suíte completa executou 24 testes sem falhas, incluindo consultas v1 sobre
  a V4, consultas agregadas evoluídas, idempotência, histórico transacional e
  incremento otimista de versão.
- Nenhum controller do fluxo evoluído foi criado e a V4 não foi aplicada no banco principal.

## 24/07/2026 - Bloco 4.3: solicitações no fluxo evoluído da API v1

- Implementado o caso de uso evoluído para criar, consultar, detalhar, aprovar,
  rejeitar e cancelar solicitações.
- A criação recebe tipo, agência, valor, motivo e data desejada; o usuário é
  obtido do JWT.
- Mantida a regra de uma solicitação aberta por agência.
- Aprovação, rejeição e cancelamento exigem justificativa e versão do agregado.
- Criados os endpoints:
  `POST/GET /api/v1/solicitacoes-numerario`,
  `GET /api/v1/solicitacoes-numerario/{id}` e
  `PUT /api/v1/solicitacoes-numerario/{id}/aprovar|rejeitar|cancelar`.
- O detalhe retorna solicitação, operação e histórico em uma resposta agregada.
- Todos os endpoints evoluídos foram protegidos para `GESTOR`; teste HTTP confirmou
  acesso do gestor e resposta `403` para operador.
- Aprovação via caso de uso foi validada com gravação do evento imutável.
- A suíte completa executou 26 testes sem falhas sobre uma cópia V4.
- A V4 não foi aplicada no banco principal e o serviço em execução não foi
  reiniciado.

## 24/07/2026 - Bloco 4.4: logística evoluída da API v1

- Implementado o fluxo de programação, separação opcional, expedição,
  registro de ocorrência, recebimento e conciliação.
- Criados os endpoints `PUT /api/v1/solicitacoes-numerario/{id}/programar`,
  `/iniciar-separacao`, `/expedir`, `/registrar-ocorrencia`, `/receber` e
  `/conciliar`, todos exclusivos de gestores.
- A programação completa a rota definida pela Tesouraria e cria uma única
  operação para a solicitação.
- A expedição bloqueia a unidade de origem, valida saldo, realiza o débito e
  grava uma movimentação imutável de saída para trânsito na mesma transação.
- O recebimento bloqueia a unidade de destino, credita somente o valor
  efetivamente recebido e grava a movimentação imutável de entrada de trânsito.
- Ocorrências são registradas no histórico sem alterar o estágio logístico.
- Comandos logísticos exigem `Idempotency-Key`; foi criada a tabela
  `comando_idempotente` para impedir efeitos duplicados, inclusive em disputa
  concorrente.
- Conflitos de versão otimista passam a responder HTTP `409`.
- Foram validados o fluxo financeiro completo de recolhimento, a repetição da
  chave de idempotência sem efeito duplo e o saldo insuficiente sem efeitos
  parciais.
- A suíte completa executou 28 testes sem falhas sobre uma cópia temporária V4.
- A V4 não foi aplicada no banco principal e o serviço em execução não foi
  reiniciado.

## 24/07/2026 - Bloco 4.5: financeiro evoluído da API v1

- Implementada a carga inicial da Tesouraria pelo endpoint exclusivo
  `POST /api/v1/tesouraria/carga-inicial`.
- A carga inicial exige valor positivo, justificativa, versão da unidade,
  `Idempotency-Key` e perfil `GESTOR`.
- A execução é permitida uma única vez. Além da validação do caso de uso, a
  tabela `comando_idempotente` ganhou uma chave única específica para proteger
  contra execuções concorrentes.
- A carga credita a unidade `TES-CENTRAL` e grava uma movimentação `AJUSTE`
  auditável, sem vínculo artificial com solicitação ou operação.
- Implementado o ajuste financeiro separado pelo endpoint
  `POST /api/v1/solicitacoes-numerario/{id}/ajustes-divergencia`.
- O ajuste exige unidade pertencente à rota, direção de entrada ou saída, valor,
  justificativa, versão da unidade e `Idempotency-Key`.
- O ajuste só é aceito para operação com divergência registrada, antes ou depois
  da conciliação documental, e seu valor não pode superar a divergência.
- A movimentação `AJUSTE_DIVERGENCIA` fica vinculada à solicitação e à operação;
  a conciliação continua sem provocar ajuste automático.
- Débito ou crédito da unidade, movimentação e registro de idempotência são
  persistidos na mesma transação.
- A suíte completa executou 30 testes sem falhas sobre uma cópia temporária V4.
- A V4 não foi aplicada no banco principal e o serviço em execução não foi
  reiniciado.

## 24/07/2026 - Bloco 5.1: consultas e contratos evoluídos do BFF

- Aprovadas todas as recomendações da Etapa 5; a execução foi dividida em três
  blocos e este registro cobre somente consultas e DTOs.
- A API passou a expor `GET /api/v1/unidades-operacionais`,
  `GET /api/v1/operacoes-numerario` e
  `GET /api/v1/solicitacoes-numerario/{id}/historico`.
- As consultas auxiliares aceitam os filtros definidos no modelo e permanecem
  exclusivas de gestores pela proteção global de `/api/v1/**`.
- O BFF ganhou contratos evoluídos próprios para solicitação, detalhe agregado,
  operação, unidade operacional e histórico.
- O BFF passou a expor e encaminhar as consultas
  `/api/v1/solicitacoes-numerario`, seu detalhe e histórico,
  `/api/v1/unidades-operacionais` e `/api/v1/operacoes-numerario`.
- JWT, filtros, datas e paginação são repassados sem alteração para a API.
- As consultas existentes foram preservadas durante a transição. As mutações evoluídas ainda
  não foram adicionadas ao BFF e pertencem ao Bloco 5.2.
- A API executou 31 testes sem falhas sobre uma cópia temporária V4.
- O BFF executou 10 testes sem falhas, incluindo o encaminhamento dos novos
  filtros e a desserialização das unidades operacionais.
- A V4 não foi aplicada no banco principal e nenhum serviço foi reiniciado.

## 24/07/2026 - Blocos 5.2 e 5.3: comandos e financeiro evoluídos do BFF

- Por solicitação do usuário, os dois blocos restantes do BFF foram executados
  em conjunto, sem novas rodadas intermediárias de aprovação.
- Implementados DTOs próprios e encaminhamento para criar, aprovar, rejeitar e
  cancelar solicitações evoluídas.
- Implementados programação, separação opcional, expedição, ocorrência,
  recebimento e conciliação.
- Implementados carga inicial exclusiva da Tesouraria e ajuste financeiro de
  divergência vinculado à solicitação.
- O BFF valida os campos estruturais e repassa o JWT, as versões dos agregados e
  a mesma `Idempotency-Key` recebida do frontend.
- O tratamento central preserva status, conteúdo e corpo dos erros da API,
  incluindo respostas `403`, `409` e `410`.
- O contrato evoluído completo do BFF foi registrado em `docs/CONTRATOS_API.md`.
- A suíte completa do BFF executou 12 testes sem falhas, incluindo consultas,
  comandos logísticos e carga inicial com propagação de idempotência.
- A API não precisou de alteração neste bloco; seus 31 testes já haviam sido
  aprovados no Bloco 5.1 sobre uma cópia V4.
- A V4 não foi aplicada no banco principal e nenhum serviço foi reiniciado.

## 24/07/2026 - Etapa 6: tela de Solicitações no frontend evoluído

- A tela deixou de usar `/api/v1/solicitacoes` e passou a consumir os contratos
  evoluídas do BFF.
- A listagem ganhou tipo da operação, novos estados, paginação e filtros por
  agência, tipo, status e período.
- A criação agora permite escolher `SUPRIMENTO` ou `RECOLHIMENTO`.
- O detalhe agregado exibe rota, estado da solicitação, estágio logístico e
  histórico auditável.
- As ações são exibidas conforme o estado: aprovação, rejeição, cancelamento,
  programação, separação, expedição, ocorrência, recebimento, conciliação e
  ajuste financeiro de divergência.
- A programação usa o seletor de unidades operacionais e o ajuste financeiro é
  limitado visualmente às unidades da rota.
- As versões atuais da solicitação, operação e unidade são encaminhadas em cada
  comando.
- Cada formulário idempotente recebe uma chave ao ser aberto e preserva a mesma
  chave em novas tentativas enquanto o formulário permanecer aberto.
- O layout foi adaptado para desktop e telas estreitas, seguindo os componentes
  e tokens visuais existentes.
- O build de produção Angular foi concluído com sucesso. Permanece somente a
  advertência preexistente de CommonJS do `exceljs`, usado no Livro Caixa.
- A execução automatizada no Chrome não encerrou dentro do limite do ambiente e
  a conexão de inspeção visual local não ficou disponível; nenhuma falha de
  compilação permaneceu.
- O banco principal e os serviços em execução não foram alterados.

## 24/07/2026 - Ativação controlada da V4

- A ativação final foi autorizada pelo usuário.
- Confirmadas as pré-condições: MySQL 8.4.9, esquema principal na V3 e serviços
  sem portas ativas antes da intervenção.
- Gerado o backup lógico
  `database/backups/gestao_numerario_pre_v4_20260724_2329.sql`, com 135.933
  bytes e SHA-256
  `22C7A52B846B995F662B771B03D822A0992083C982D9F003ADD50F272B6A3566`.
- O backup foi restaurado em banco temporário e validou 176 solicitações, 540
  movimentações, saldo agregado de R$ 11.260.000,00 e Flyway V3. A base de
  verificação foi removida depois da conferência.
- O rascunho aprovado foi promovido para
  `api-numerario/src/main/resources/db/migration/V4__evolucao_solicitacao_numerario.sql`.
- A API aplicou a V4 pelo Flyway com sucesso.
- A validação pós-migração confirmou 176 solicitações, 540 movimentações, 90
  operações, 424 eventos, 31 unidades de agência e saldo agregado preservado em
  R$ 11.260.000,00.
- A Tesouraria Central foi criada ativa, com controle de saldo e saldo inicial
  zero. Não foram encontradas solicitações de suprimento sem destino nem
  movimentações sem unidade operacional.
- API, BFF e frontend foram iniciados, respectivamente, nas portas 8081, 8080 e
  4200.
- Login pelo frontend/proxy, consulta evoluída paginada, unidades operacionais e
  detalhe agregado foram validados por HTTP. A consulta retornou as 176
  solicitações migradas.
- Recuperação, se necessária, exige parar os serviços, recriar o banco
  `gestao_numerario` e restaurar o backup lógico registrado acima.

## 24/07/2026 - Breadcrumb contextual do Dashboard

- Identificado que Agências e Solicitações montavam breadcrumbs fixos e não
  recebiam informação sobre a origem da navegação.
- Os atalhos de pendências do Dashboard passaram a enviar
  `origem=dashboard` junto dos filtros existentes.
- Quando abertas por esses atalhos, as telas exibem
  `COIN Home > Tesouraria > Dashboard > Agências/Solicitações` e o botão voltar
  retorna ao Dashboard.
- Acesso direto ou pelo menu da Tesouraria preserva o breadcrumb estrutural
  anterior e o retorno para `/tesouraria`.
- O build de produção Angular foi concluído com sucesso.

## 25/07/2026 - Correção de Agências e Movimentações após V4

- Corrigida `LazyInitializationException` na consulta de Agências: a unidade
  operacional, que passou a armazenar o saldo na V4, agora é carregada junto
  com a agência nas consultas por página, identificador, bloqueio e lista.
- A API foi recompilada e reiniciada; a chamada real pelo proxy do frontend
  retornou com sucesso as 14 agências atualmente em alerta.
- O atalho de Movimentações no Dashboard também passou a enviar o contexto de
  origem por query parameter e estado de navegação.
- Movimentações agora exibe
  `COIN Home > Tesouraria > Dashboard > Movimentações` quando aberta pelo
  Dashboard, e o botão voltar retorna para `/dashboard`.
- O frontend em desenvolvimento recompilou e recarregou a página sem erros.

## 25/07/2026 - Separação do módulo de Solicitações

- `/solicitacoes` passou a ser uma entrada do módulo com dois cards:
  `Consultar solicitações` e `Fazer nova solicitação`.
- A consulta, seus filtros, detalhe agregado e ações operacionais foram movidos
  para `/solicitacoes/consultar`.
- O formulário de criação ganhou tela exclusiva em `/solicitacoes/nova`.
- Após cadastrar, a navegação segue para a consulta e apresenta a confirmação de
  sucesso.
- Breadcrumbs e botões voltar passaram a incluir o nível `Solicitações`.
- O atalho de pendências do Dashboard foi atualizado para abrir diretamente a
  consulta filtrada, preservando `Dashboard` no breadcrumb.
- O build de produção foi concluído com sucesso e o servidor de desenvolvimento
  recarregou a página.

## 27/07/2026 - Migração do coin-2 preservando a API v1

- Incorporada a evolução funcional do repositório `coin-2`.
- Todos os contratos HTTP foram consolidados sob `/api/v1`; não foi criada uma
  segunda versão da API.
- Os endpoints existentes de escrita para agências, movimentações, solicitações
  e usuários permaneceram ativos.
- O dump citado no histórico do projeto de origem não foi importado, pois
  contém dados de uma base específica.
- A execução automática do Flyway foi desabilitada. V4 a V6 só são promovidas
  pelo procedimento `scripts/migrar-banco-seguro.ps1`, após preflight em V3 e
  dump lógico validado.
- Um primeiro ensaio em base temporária detectou incompatibilidade de collation
  na V4 antes de qualquer alteração na base principal. A comparação de códigos
  foi corrigida e o ensaio seguinte concluiu V4 a V6.
- A base principal recebeu um novo backup lógico validado, armazenado somente
  em `database/backups-local`, e foi migrada de Flyway V3 para V6.
- A validação final confirmou 34 agências, 36 unidades operacionais, 182
  solicitações, 92 operações, 437 eventos e 545 movimentações.
- Permaneceram em zero: agências sem unidade, suprimentos sem destino e
  movimentações sem unidade operacional.
- API e BFF compilaram; 67 testes seguros da API, 39 testes do BFF e 41 testes
  Angular foram concluídos sem falhas.
