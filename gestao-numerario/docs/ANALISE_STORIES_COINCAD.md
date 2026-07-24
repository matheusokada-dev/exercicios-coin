# Analise das stories do COINCAD

Atualizado em: 23/07/2026.

## Escopo confirmado em 23/07/2026

- Adotar os perfis `COIN0001` a `COIN0006`.
- Manter uma camada de compatibilidade durante a transição: `GESTOR -> COIN0001` e `OPERADOR -> COIN0003`.
- Criar COIN Home com os módulos Tesouraria e Cadastros.
- Tesouraria direciona para o sistema já desenvolvido.
- Cadastros direciona para a página de erro enquanto o módulo não estiver desenvolvido.
- Consulta de pontos, importação e relatórios permanecem requisitos pendentes; não foram implementados.

## Status deste documento

Este documento registra a analise das stories fornecidas pelo usuario. As stories sao fonte de requisito, mas as propostas tecnicas e os pontos em aberto abaixo ainda precisam de confirmacao antes da implementacao.

Objetivo: permitir a retomada do trabalho sem reler integralmente os anexos ou depender do historico da conversa.

## Fase atual confirmada

Antes de expandir o dominio COINCAD, a massa de desenvolvimento sera criada somente para a estrutura ja implementada: usuarios, agencias, solicitacoes de abastecimento e movimentacoes.

Cliente, Ponto, Cofre e Frequencia nao fazem parte desta primeira massa porque ainda nao existem no modelo de dados.

## Conclusao executiva

As stories cabem na arquitetura atual de frontend Angular, BFF Spring MVC e API hexagonal, mas nao cabem no modelo funcional existente sem uma expansao relevante.

O sistema atual e um MVP de saldo, movimentacao e abastecimento de agencias. As novas stories introduzem o dominio COINCAD, responsavel pelos cadastros mestres que alimentam os processos de Tesouraria.

O COINCAD da contexto para Cliente, Ponto, Cofre Inteligente, Custodiante, TecBan, Entre Bases e frequencias ciclicas. Ele nao implementa sozinho Livro Caixa, MCIR, NUME, RENU ou conciliacao fisico versus contabil.

## Stories analisadas

| ID interno | Story | Cobertura atual |
| --- | --- | --- |
| CAD-AUTH | Autenticacao corporativa pelo GIDE e primeiro acesso | Parcial e conflitante com a implementacao local |
| CAD-PONTO | Consulta e lista de pontos de clientes | Nao atendida |
| CAD-HIST | Exportacao Excel do historico de cliente | Nao atendida |
| CAD-IMPORT | Importacao massiva XLSX de clientes | Nao atendida |
| CAD-REL | Relatorio consolidado de clientes e totalizadores | Nao atendida |

## Evidencias no sistema atual

- A autenticacao valida login e senha armazenados localmente e emite JWT proprio.
- A tabela `usuario` exige `senha_hash`.
- Existe criacao manual de usuario pela API.
- Os unicos perfis sao `GESTOR` e `OPERADOR`.
- O frontend possui apenas login, dashboard, agencias, solicitacoes e movimentacoes.
- O banco possui somente `usuario`, `agencia`, `solicitacao_abastecimento` e `movimentacao`.
- Nao ha dependencia ou componente para GIDE/OIDC, leitura ou geracao XLSX.
- Nao existem entidades de Cliente, Contrato, Ponto, Cofre, Frequencia, Historico cadastral ou Importacao.

Arquivos principais usados na verificacao:

- `api-numerario/src/main/java/br/com/gestaonumerario/api/core/usecase/autenticacao/AutenticacaoService.java`
- `api-numerario/src/main/java/br/com/gestaonumerario/api/core/domain/model/Usuario.java`
- `api-numerario/src/main/java/br/com/gestaonumerario/api/core/domain/enums/PerfilUsuario.java`
- `api-numerario/src/main/java/br/com/gestaonumerario/api/config/SecurityConfig.java`
- `api-numerario/src/main/resources/db/migration/V1__create_schema.sql`
- `frontend-numerario/src/app/app.routes.ts`
- `frontend-numerario/src/app/core/auth.service.ts`

## Escopo necessario

### Fundacao de identidade

- Definir o contrato real do GIDE: OIDC, OAuth2, SAML ou API corporativa.
- Definir se o navegador sera redirecionado ao GIDE ou se havera reautenticacao especifica ao entrar no modulo Cadastros.
- Criar uma porta de identidade corporativa e seu adapter.
- Remover a dependencia funcional de senha local.
- Provisionar o usuario no primeiro acesso sem duplicidade.
- Persistir ID corporativo ou matricula, nome, e-mail, status e perfil interno.
- Validar usuario ativo, expiracao de sessao e indisponibilidade do GIDE.
- Registrar auditoria de acesso com data, usuario, origem e resultado.
- Mapear os perfis `COIN0001` a `COIN0006` e aplicar autorizacao tambem no backend.

### Dominio COINCAD

O modelo precisa representar, no minimo:

- `ClienteNumerario` e seu contrato bancario.
- `PontoAtendimentoNumerario`.
- `CofrePontoAtendimento`.
- `FrequenciaAtendimento`, origem das operacoes ciclicas.
- Empresas de servico: custodiante, tesouraria, processadora e transportadora.
- Historico imutavel de Cliente e Ponto.
- Lote de importacao e erros por linha.
- Auditoria de inclusao e manutencao.

Os nomes fisicos legados descritos nas stories devem ficar isolados nos adapters de persistencia. O dominio deve usar nomes de negocio legiveis.

### Frontend e BFF

- Criar COIN Home com os acessos Tesouraria e Cadastros.
- Criar o menu COINCAD e controlar sua visualizacao por perfil.
- Implementar consulta de pontos com filtros combinados, autocomplete e paginacao de 50 registros.
- Implementar selecao de linha e navegacao para consultar, incluir, alterar, historico e relatorio.
- Implementar upload XLSX, loading, resumo da importacao e acesso ao log.
- Implementar download dos relatorios Excel.
- Garantir navegacao por teclado e compatibilidade com Chrome e Edge.

### Integracoes externas

- GIDE.
- `ctas-srv-dados-conta`.
- Servico que valida o vinculo entre CNPJ e conta.
- `cepn-srv-endereco`.
- Fontes de dados de empresas, custodiante e demais informacoes cadastrais.

Cada integracao deve ter contrato, timeout, tratamento de indisponibilidade, logs e massa de teste definidos.

## Pontos de refinamento obrigatorios

1. A story pede SSO, mas tambem pede uma nova tela de login e senha ao entrar em Cadastros. Definir o fluxo oficial.
2. Definir se a inativacao no GIDE bloqueia apenas o proximo login ou invalida imediatamente uma sessao existente.
3. Retirar da story de autenticacao as regras de filtro e listagem de clientes, pois pertencem ao Cadastro de Ponto.
4. No historico, escolher entre gerar Excel com o cadastro atual quando nao houver alteracoes ou exibir apenas "Cliente nao tem historico".
5. Na importacao, resolver o conflito entre "todos os campos obrigatorios" e "dados ausentes recuperados internamente".
6. Definir a chave natural de Cliente: agencia e conta, ou agencia, conta e CNPJ.
7. Definir processamento atomico por arquivo ou parcial por linha. O resumo com sucessos e erros sugere transacao por linha.
8. Definir idempotencia e comportamento ao reenviar o mesmo arquivo.
9. Definir tamanho maximo, quantidade de linhas, formato de datas/percentuais e regras para arquivos corrompidos.
10. Incluir ou justificar a ausencia de Cofre Inteligente no filtro de tipo do relatorio.
11. Corrigir a regra de vigencia dos convenios. A story conta registros cuja data de termino e diferente de `NULL`, o que pode representar convenio encerrado.
12. Definir se a quantidade de cofres considera todos os registros ou apenas relacionamentos vigentes.
13. Definir o comportamento sem dados: somente mensagem ou mensagem mais arquivo zerado.
14. Fornecer o layout oficial da story COIN-5795.
15. Definir volume esperado e se importacoes e relatorios serao sincronos ou processados como jobs.

## Dependencias entre as stories

Ordem recomendada:

1. Contrato GIDE, sessao, usuario e perfis.
2. Modelo de dados COINCAD e migrations Flyway.
3. COIN Home, menu Cadastros e autorizacao.
4. Cadastro/consulta de Cliente e Ponto.
5. Versionamento e historico cadastral.
6. Importacao massiva e log de erros.
7. Relatorio historico.
8. Relatorio consolidado e totalizadores.
9. Testes de carga, seguranca, acessibilidade e compatibilidade.

CAD-HIST, CAD-IMPORT e CAD-REL nao devem ser iniciadas antes da estabilizacao do modelo de Cliente e Ponto.

## Direcao arquitetural proposta

Manter a arquitetura hexagonal:

- Ports de entrada por intencao: autenticar, consultar pontos, importar clientes e gerar relatorios.
- Ports de saida para GIDE, contas, enderecos, persistencia, auditoria e geracao de planilha.
- Regras de validacao, vigencia, inativacao em cascata e totalizadores no core.
- HTTP, JPA, bibliotecas XLSX e clientes corporativos somente nos adapters.
- BFF responsavel pela jornada do navegador e pela composicao adequada ao frontend.

Essa direcao e uma proposta. A decisao entre manter o COINCAD na API atual ou criar um servico separado ainda nao foi tomada.

## Regra de manutencao

Ao confirmar uma decisao de refinamento:

- Atualizar este documento.
- Promover o requisito aplicavel para `REQUISITOS.md`.
- Registrar entidades confirmadas em `MODELO_DADOS.md`.
- Registrar endpoints confirmados em `CONTRATOS_API.md`.
- Registrar implementacoes realizadas em `DIARIO_IMPLEMENTACAO.md`.
