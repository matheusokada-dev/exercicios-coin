# Contexto e decisões

## Como usar esta documentação

Este arquivo é o resumo de retomada. Antes de implementar, ler também o documento técnico da etapa: banco em `MODELO_DADOS.md`, endpoints em `CONTRATOS_API.md` e escopo em `REQUISITOS.md`.

Alterações de interface devem consultar `PADRAO_FRONTEND_BRADESCO.md`. O guia
separa regras extraídas do Liquid Bradesco, adaptações Angular e lacunas ainda
dependentes de documentação oficial.

Para uma visão consolidada e navegável, abrir `DOCUMENTACAO_COMPLETA.html`.

## Ordem de precedência

1. Pedido do gestor.
2. Confirmações explícitas do usuário nesta conversa.
3. Guia técnico fornecido pelo usuário.
4. Propostas do assistente, sempre identificadas como propostas.

## Fontes de referência

- Enunciado do gestor: anexo `pasted-text.txt` fornecido pelo usuário.
- Guia técnico: `C:\Users\MatheusKazuoOkada\Downloads\guia_sistema_gestao_numerario.html`.
- O guia define o modelo relacional de referência e regras de negócio; sua sintaxe SQL Server deve ser adaptada para MySQL.

## Contexto COINCAD em refinamento

- As stories de autenticação GIDE, consulta de pontos, histórico, importação massiva e relatório de clientes foram analisadas.
- O sistema atual comporta a expansão arquitetural, mas ainda não possui o domínio COINCAD.
- Contradições e dependências que precisam de decisão estão consolidadas em `ANALISE_STORIES_COINCAD.md`.
- Nada descrito como proposta nesse documento deve ser tratado como decisão confirmada.

## Arquitetura confirmada

| Componente | Tecnologia | Estilo | Porta |
| --- | --- | --- | --- |
| `frontend-numerario` | Angular 19 | SPA | 4200 |
| `bff-numerario` | Java/Spring Boot | MVC/BFF | 8080 |
| `api-numerario` | Java/Spring Boot 3.5.14 | Hexagonal | 8081 |
| Banco | MySQL 8.4.5 | Local | 3306 |

## Convenções confirmadas

- GroupId: `br.com.gestaonumerario`.
- Pacote-base da API: `br.com.gestaonumerario.api`.
- Java 21, Maven e JAR.
- Banco local: `gestao_numerario`.
- Credenciais locais ficam somente em `application-local.properties`, arquivo ignorado pelo Git. O modelo versionado é `application-local.properties.example`; nenhuma senha é documentada.
- Flyway é o único responsável por evoluir o esquema. Hibernate usa `ddl-auto=validate`.
- A conexão JDBC e o Hibernate usam UTC para valores temporais.
- A pasta raiz `database` é para material de apoio. Migrations executáveis ficam em `api-numerario/src/main/resources/db/migration`.

## Estado técnico confirmado

- API base compila em referência anterior com `./mvnw.cmd clean compile`.
- API conecta ao MySQL e inicia em 8081 com o profile `local` padrão, após preencher `application-local.properties`.
- Migrations V1, V2 e V3 existem.
- JWT Bearer foi implementado na API, com validade de 60 minutos e sem refresh token.
- O login bloqueia a conta por 15 minutos após cinco senhas incorretas consecutivas e persiste esse controle no MySQL.
- O primeiro gestor já foi criado diretamente no MySQL.
- BFF encaminha login e chamadas autenticadas para a API.
- A raiz do frontend abre o login; após autenticação, o usuário é direcionado ao COIN Home.
- O formulário de login usa template HTML separado e valida os campos obrigatórios antes de chamar o BFF.
- Frontend possui COIN Home separado em Tesouraria/Cadastros, rotas protegidas, loading global, toast de sucesso somente para alterações, toast de erro e página de erro centralizada.
- Valores monetários exibidos pelo Angular usam `CurrencyPipe` com locale `pt-BR` e moeda `BRL`.
- O padrão visual do frontend passa a seguir `PADRAO_FRONTEND_BRADESCO.md`; tipografia, paginação, donut, alert e modal possuem referência recebida, enquanto Card e paleta institucional completa continuam pendentes de fonte oficial.
- A camada frontend reconhece `COIN0001` a `COIN0006` e mapeia temporariamente `GESTOR/OPERADOR` para `COIN0001/COIN0003`.
- Tesouraria abre um menu com Dashboard, Solicitações, Agências, Movimentações e Livro Caixa.
- Cadastros abre a página de erro porque ainda não foi desenvolvido.
- O header autenticado possui somente logout; telas internas usam breadcrumbs e botão Voltar.
- Livro Caixa gera XLSX por agência e período usando os endpoints existentes de agências e movimentações. A rota exige gestor porque a listagem de agências já possui essa restrição na API.
- Consulta de pontos, importação e relatórios XLSX não foram implementados.
- A massa local V1 possui 7 usuários, 30 agências, 176 solicitações e 540 movimentações identificadas; composição e validações estão em `MASSA_DADOS.md`.
- O build de produção do frontend foi validado em 23/07/2026.

## Próxima ação

Executar validação manual do fluxo pelo navegador:

1. Subir API, BFF e frontend.
2. Aplicar `database/scripts/upsert-gestor-dev.sql` para garantir a credencial local conhecida.
3. Aplicar `database/scripts/seed-dados-dev.sql` no MySQL local, se for necessário carregar dados de exemplo.
4. Fazer login com `gestor` / `admin123`.
5. Conferir se o token é salvo e enviado nas chamadas.
6. Navegar pelo COIN Home e menu de Tesouraria.
7. Conferir dashboard, agências, solicitações, movimentações e Livro Caixa.
8. Ajustar os problemas encontrados nas telas e nos contratos BFF/API.

Com PowerShell, uma forma prática de aplicar o seed é:

```powershell
Get-Content .\database\scripts\upsert-gestor-dev.sql | mysql -u root -p gestao_numerario
Get-Content .\database\scripts\seed-dados-dev.sql | mysql -u root -p gestao_numerario
```

A credencial `gestor` / `admin123` é exclusiva para desenvolvimento local e não deve ser usada em outros ambientes.
