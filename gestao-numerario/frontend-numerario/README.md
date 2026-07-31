# Frontend Numerário

Aplicação Angular 19 da Gestão de Numerário.

## Fluxo atual

- A raiz `/` redireciona para a tela de login em `/login`.
- Rotas internas protegidas por `authGuard`.
- COIN Home em `/menu`, separando Tesouraria e Cadastros.
- Tesouraria direciona para `/tesouraria`, com cards para Dashboard, Solicitações, Agências e Livro Caixa.
- Cadastros direciona para a página de erro porque o módulo ainda não foi desenvolvido.
- O header autenticado contém somente o botão Sair.
- Telas internas possuem breadcrumbs e botão Voltar conforme a hierarquia de navegação.
- Livro Caixa em `/livro-caixa`: seleciona agência e período e solicita ao BFF
  a geração centralizada do relatório `.xlsx`.
- Access token salvo no `localStorage` na chave `coin.accessToken`; o resumo da
  sessão usa `coin.sessao`.
- Interceptor HTTP envia `Authorization: Bearer <token>` nas chamadas ao BFF.
- Interceptor global exibe loading e toast em chamadas HTTP.
- Rotas de agência usam `gestorGuard`.
- A página `/erro` trata URL inválida, acesso indevido e indisponibilidade da API/BFF.
- Rotas disponíveis: menu, tesouraria, dashboard, agências, detalhe da agência,
  solicitações e livro caixa.

## Livro Caixa

O frontend usa os seguintes contratos:

- `GET /api/v1/agencias` para seleção da agência.
- `POST /api/v1/relatorios/livro-caixa` para solicitar o relatório.

O BFF consulta todas as páginas de movimentações na API, monta colunas, linhas,
totais e metadados e chama o `relatorio-numerario`. O frontend apenas converte
o Base64 retornado em arquivo para download. Como a listagem de agências exige
perfil gestor na API atual, `/livro-caixa` usa `gestorGuard`.

## Perfis COIN

O frontend reconhece os perfis `COIN0001` a `COIN0006`.

- Compatibilidade local temporária: `GESTOR` é interpretado como `COIN0001`; `OPERADOR` como `COIN0003`.

O backend continua persistindo e emitindo `GESTOR/OPERADOR`.

## Desenvolvimento local

O frontend deve consumir o BFF pelas rotas relativas `/api/v1`.

Para iniciar todo o ambiente local, usar o script da raiz do projeto:

```bash
scripts/iniciar-tudo.ps1
```

Portas esperadas:

- Frontend: `http://localhost:4200`
- BFF: `http://localhost:8080`
- API: `http://localhost:8081`

Validação do frontend:

```powershell
npm ci
npx tsc -p tsconfig.app.json --noEmit --noUnusedLocals --noUnusedParameters
npm run build
```

Não há suíte automatizada de frontend no estado atual.

## Dados de exemplo

Antes do seed, crie ou redefina o gestor local com uma senha conhecida:

```powershell
Get-Content .\database\scripts\upsert-gestor-dev.sql | mysql -u root -p gestao_numerario
```

Credencial local: login `gestor`, senha `admin123`.

Depois, se o banco estiver vazio, aplique o seed local:

```powershell
Get-Content .\database\scripts\seed-dados-dev.sql | mysql -u root -p gestao_numerario
```

O seed cria usuários locais e popula agências, solicitações e movimentações para o dashboard e as listas do frontend. A senha fixa é somente para desenvolvimento local.

## Pendências

- Evoluir formulários de agências e solicitações.
- Substituir IDs de agência digitados manualmente por seleção pesquisável.
