# Contexto e decisões

## Como usar esta documentação

Este arquivo é o resumo de retomada. Antes de implementar, ler também o documento técnico da etapa: banco em `MODELO_DADOS.md`, endpoints em `CONTRATOS_API.md` e escopo em `REQUISITOS.md`.

Alterações de interface devem consultar `PADRAO_FRONTEND_BRADESCO.md`. O guia
separa regras extraídas do Liquid Bradesco, adaptações Angular e lacunas ainda
dependentes de documentação oficial.

Para a documentação navegável, abrir `DOCUMENTACAO_COMPLETA.html`. Esse arquivo
é o índice para quatro documentos independentes: frontend `4200`, BFF `8080`,
API `8081` e relatórios `8082`. Cada documento explica o fluxo da aplicação e
seu catálogo de classes, arquivos e métodos.

Com API e BFF em execução, `node scripts/validar-openapi.mjs` verifica se todos
os controllers e operações continuam documentados no OpenAPI.

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
- A baseline Flyway possui `V1__create_schema.sql` (estrutura). A massa local
  opcional fica em `database/scripts/seed-dados-dev.sql`, sem unidade operacional ou
  tabela de refresh token.
- JWT Bearer foi implementado na API, com validade de 8 horas e sem refresh token.
- O login bloqueia a conta por 15 minutos após cinco senhas incorretas consecutivas e persiste esse controle no MySQL.
- O primeiro gestor já foi criado diretamente no MySQL.
- BFF encaminha login e chamadas autenticadas para a API.
- A raiz do frontend abre o login; após autenticação, o usuário é direcionado ao COIN Home.
- O formulário de login usa template HTML separado e valida os campos obrigatórios antes de chamar o BFF.
- Frontend possui COIN Home separado em Tesouraria/Cadastros, rotas protegidas, loading global, toast de sucesso somente para alterações, toast de erro e página de erro centralizada.
- Valores monetários exibidos pelo Angular usam `CurrencyPipe` com locale `pt-BR` e moeda `BRL`.
- O padrão visual do frontend passa a seguir `PADRAO_FRONTEND_BRADESCO.md`; tipografia, paginação, donut, alert e modal possuem referência recebida, enquanto Card e paleta institucional completa continuam pendentes de fonte oficial.
- A camada frontend reconhece `COIN0001` a `COIN0006` e mapeia temporariamente `GESTOR/OPERADOR` para `COIN0001/COIN0003`.
- OpenAPI concluído no padrão `interface *Api` → `Controller implements *Api`:
  34 operações na API e 32 no BFF, com bearer JWT, login público, tags,
  descrições e respostas comuns.
- Tesouraria abre um menu com Dashboard, Solicitações, Agências e Livro Caixa.
- Cadastros abre a página de erro porque ainda não foi desenvolvido.
- O header autenticado possui somente logout; telas internas usam breadcrumbs e botão Voltar.
- Livro Caixa gera XLSX por agência e período. O BFF consulta agências e
  movimentações, monta o contrato tabular e chama o `relatorio-numerario` na
  porta `8082`; o frontend apenas baixa o Base64 retornado.
- A rota de Livro Caixa exige gestor porque a listagem de agências já possui
  essa restrição na API.
- Consulta de pontos, importação e os demais relatórios XLSX não foram
  implementados.
- A V1 cria o schema; o seed manual cria credenciais e dados de exemplo locais.
- O build de produção do frontend foi validado em 23/07/2026.

## Próxima ação

Executar validação manual do fluxo pelo navegador:

1. Subir API, serviço de relatórios, BFF e frontend.
2. Em um banco resetado, habilitar Flyway uma vez para aplicar a V1.
3. Somente em desenvolvimento, aplicar `database/scripts/seed-dados-dev.sql`.
4. Confirmar a credencial local criada pelo seed.
4. Fazer login com `gestor` / `admin123`.
5. Conferir se o token é salvo e enviado nas chamadas.
6. Navegar pelo COIN Home e menu de Tesouraria.
7. Conferir dashboard, agências, solicitações e Livro Caixa.
8. Ajustar os problemas encontrados nas telas e nos contratos BFF/API.

Com PowerShell, uma forma prática de aplicar as migrations é:

```powershell
$env:FLYWAY_ENABLED='true'
.\api-numerario\mvnw.cmd -f .\api-numerario\pom.xml spring-boot:run
```

A credencial `gestor` / `admin123` é exclusiva para desenvolvimento local e não deve ser usada em outros ambientes.
