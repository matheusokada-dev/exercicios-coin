# Arquitetura da API

## Estilo adotado

A API usa arquitetura hexagonal com três limites explícitos:

- `core`: domínio e casos de uso, sem dependência de HTTP, JPA ou Spring.
- `port`: contratos que conectam o core ao exterior.
- `adapter`: implementações e entradas externas, como REST, persistência e tarefas agendadas.

## Estrutura oficial

```text
br.com.gestaonumerario.api
├── adapter
│   ├── input
│   │   ├── rest
│   │   └── scheduler
│   └── output
│       ├── persistence
│       ├── security
│       └── time
├── config
├── core
│   ├── domain
│   │   ├── enums
│   │   └── model
│   ├── exception
│   └── usecase
├── port
│   ├── input
│   └── output
└── ApiNumerarioApplication
```

## Responsabilidades

| Área | Responsabilidade |
| --- | --- |
| `core/domain/model` | Entidades, value objects e regras puras de negócio. |
| `core/domain/enums` | Enumerações do domínio: perfil, status, tipo de movimentação e ordenação. |
| `core/usecase` | Implementação dos casos de uso. |
| `core/exception` | Exceções de regra de negócio. |
| `port/input` | Interfaces dos casos de uso que o exterior pode chamar. |
| `port/output` | Interfaces que o core precisa para persistir, autenticar, gerar token ou obter tempo. |
| `adapter/input/controller` | Controllers, DTOs HTTP e conversores de entrada/saída. |
| `adapter/output/persistence` | Entidades JPA, Spring Data repositories, mappers e adapters de persistência. |
| `adapter/output/security` | JWT e codificação de senha. |
| `adapter/output/time` | Relógio UTC. |
| `config` | Beans, segurança e composição técnica do Spring. |

## Regras de dependência

- `core` não importa classes de `adapter`, Spring, JPA ou HTTP.
- `port` referencia apenas tipos do `core`.
- `adapter` implementa `port` ou chama `port`; não contém regra central de negócio.
- `config` é a camada de composição e pode ligar adapters, ports e core.
- Transações são acessadas pelo core por `TransacaoOutputPort`; a implementação Spring fica fora do domínio.

## Granularidade dos casos de uso

- Os contratos de entrada continuam separados por intenção.
- Enquanto operações de solicitação pertencerem ao mesmo agregado e compartilharem dependências, `SolicitacaoUseCase` implementará solicitar, aprovar, rejeitar, atender e consultar.
- A separação em várias services só será feita quando houver dependências, regras ou ritmo de mudança realmente distintos.

## Persistência JPA

- Entidades JPA têm sufixo `Entity` e ficam em `adapter/output/repository/entity`.
- Repositórios Spring Data ficam em `adapter/output/repository`.
- Mappers entre entidades JPA e modelos de domínio ficam em `adapter/output/mapper`.
- Nenhuma anotação JPA deve ser adicionada aos modelos do `core`.
