# Gestão de Numerário

Este é o ponto de entrada da documentação do projeto. Para retomar o trabalho, comece por [docs/LEIA_PRIMEIRO.md](docs/LEIA_PRIMEIRO.md).

Documento consolidado e navegável: [docs/DOCUMENTACAO_COMPLETA.html](docs/DOCUMENTACAO_COMPLETA.html).

## Estado atual

- API criada em `api-numerario`, com Java 21, Maven, Spring Boot 3.5.14 e arquitetura hexagonal.
- Migrations Flyway V1 a V6 disponíveis; V4 a V6 permanecem desabilitadas por
  padrão até a execução do procedimento de backup e migração segura.
- Autenticação com JWT curto, refresh token rotativo, cookies HttpOnly e CSRF.
- Login protegido por bloqueio de 15 minutos após cinco senhas incorretas consecutivas.
- Primeiro usuário gestor já foi criado diretamente no MySQL.
- BFF criado em `bff-numerario`, porta 8080, encaminhando chamadas para a API na porta 8081.
- Frontend Angular com login, dashboard, agências, solicitações, operações,
  movimentações, tesouraria e livro-caixa.
- Contratos existentes e fluxo evoluído mantidos sob o prefixo `/api/v1`.
- Script local `scripts/iniciar-tudo.ps1` criado para iniciar API, BFF e frontend.

## Próxima etapa

Validar o fluxo completo após a migração local concluída em Flyway V6:

1. Iniciar API, BFF e frontend.
2. Acessar `/login`.
3. Conferir dashboard e contratos existentes.
4. Validar solicitações, operações, movimentações e tesouraria.
5. Registrar eventuais ajustes encontrados na navegação.

Para migrar outro ambiente, repetir integralmente
`docs/MIGRACAO_BANCO_SEGURA.md`; o backup local não deve ser reutilizado.

## Documentos

- [Padrão de frontend Bradesco](docs/PADRAO_FRONTEND_BRADESCO.md)
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
