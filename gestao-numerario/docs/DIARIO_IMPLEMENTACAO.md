# Diário de implementação

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
