# Migração segura do banco — V3 para V6

As migrations V4, V5 e V6 acompanham a evolução da gestão de numerário. A V4
transforma estruturas existentes e contém renomeações, remoções de colunas,
índices e constraints. Como o MySQL realiza commit implícito para várias
instruções DDL, a aplicação não executa essas migrations automaticamente.

## Pré-condições

- Janela de manutenção com API e BFF parados.
- Banco confirmado na versão Flyway V3.
- `mysql` e `mysqldump` disponíveis no `PATH`.
- Espaço livre para o dump lógico.
- Credenciais exclusivas do ambiente que será migrado.

Nunca execute o procedimento primeiro em produção. Valide o dump e a migration
em uma instância temporária com cópia representativa dos dados.

## Preflight e backup

Configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` e execute:

```powershell
.\scripts\migrar-banco-seguro.ps1
```

O script:

1. confirma que o Flyway está na versão 3;
2. registra a quantidade de agências, movimentações, solicitações e usuários;
3. cria um dump com transação consistente, procedures, triggers e events;
4. valida que o arquivo não está vazio;
5. encerra sem aplicar migrations.

Backups locais são gravados em `database/backups-local` e ignorados pelo Git.

## Aplicação controlada

Depois de restaurar e validar o backup em uma instância temporária:

```powershell
.\scripts\migrar-banco-seguro.ps1 -Aplicar
```

Somente essa execução habilita temporariamente `FLYWAY_ENABLED=true`. Ao final,
o script exige que `flyway_schema_history` esteja na versão 6.

## Recuperação

Se a execução falhar, não tente editar `flyway_schema_history` nem reaplicar
trechos da V4 manualmente. Preserve logs e o dump, recrie o banco e restaure o
backup lógico antes de investigar a causa.

O backup não é versionado porque pode conter dados pessoais, hashes e
credenciais de massa.
