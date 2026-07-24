# Gestão de Numerário

Este é o ponto de entrada da documentação do projeto. Para retomar o trabalho, comece por [docs/LEIA_PRIMEIRO.md](docs/LEIA_PRIMEIRO.md).

Documento consolidado e navegável: [docs/DOCUMENTACAO_COMPLETA.html](docs/DOCUMENTACAO_COMPLETA.html).

## Estado atual

- API criada em `api-numerario`, com Java 21, Maven, Spring Boot 3.5.14 e arquitetura hexagonal.
- MySQL 8.4.5 local e banco `gestao_numerario` validados.
- Migrations Flyway V1, V2 e V3 criadas; a V3 persiste tentativas inválidas e bloqueio temporário de login.
- Autenticação JWT implementada na API e encaminhada pelo BFF.
- Login protegido por bloqueio de 15 minutos após cinco senhas incorretas consecutivas.
- Primeiro usuário gestor já foi criado diretamente no MySQL.
- BFF criado em `bff-numerario`, porta 8080, encaminhando chamadas para a API na porta 8081.
- Frontend Angular criado em `frontend-numerario`, com login, dashboard, agências, solicitações e movimentações em versão inicial.
- Script local `scripts/iniciar-tudo.ps1` criado para iniciar API, BFF e frontend.

## Próxima etapa

Validar manualmente o fluxo completo pelo frontend:

1. Iniciar API, BFF e frontend.
2. Popular dados locais com `database/scripts/seed-dados-dev.sql`, se o banco ainda não tiver agências, solicitações e movimentações.
3. Acessar `/login`.
4. Entrar com o gestor já criado.
5. Conferir dashboard.
6. Navegar por agências, solicitações e movimentações.
7. Registrar ajustes necessários encontrados na navegação.

Compilações e testes automatizados ficam pendentes por decisão do usuário nesta etapa.

## Documentos

- [Massa de dados de desenvolvimento](docs/MASSA_DADOS.md)
- [Análise das stories do COINCAD](docs/ANALISE_STORIES_COINCAD.md)
- [Contexto e decisões](docs/LEIA_PRIMEIRO.md)
- [Arquitetura da API](docs/ARQUITETURA.md)
- [Requisitos rastreáveis](docs/REQUISITOS.md)
- [Modelo de dados](docs/MODELO_DADOS.md)
- [Contratos de API](docs/CONTRATOS_API.md)
- [Diário de implementação](docs/DIARIO_IMPLEMENTACAO.md)

## Regra de manutenção

Antes de uma nova etapa, consultar os documentos aplicáveis. Após a etapa, atualizar o diário e o documento técnico afetado. Nada que seja apenas uma proposta será tratado como decisão confirmada sem aprovação explícita do usuário.
