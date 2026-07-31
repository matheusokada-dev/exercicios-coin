# Massa de dados

A massa local fica fora do Flyway em
`database/scripts/seed-dados-dev.sql`. Ela é aplicada manualmente, somente em
desenvolvimento, depois que a V1 tiver criado um schema vazio.

## Credenciais

Todos os usuários usam a senha `admin123`.

- `gestor` e `gestor.aprovador`: perfil `GESTOR`;
- `operador.sp`, `operador.sul`, `operador.nordeste` e `operador.centro`:
  perfil `OPERADOR`;
- `operador.inativo`: cenário de usuário inativo.

Essas credenciais são exclusivas para desenvolvimento.

## Cobertura

- 7 usuários: 2 gestores, 4 operadores ativos e 1 operador inativo;
- 30 agências: 26 ativas e 4 inativas;
- 11 agências ativas abaixo do limite e 2 exatamente no limite;
- 14 solicitações: duas de cada status, entre suprimento e recolhimento;
- 8 operações cobrindo todos os seis estados operacionais;
- 14 movimentações com débito na origem, crédito no destino, saque, depósito e
  ajuste;
- 26 eventos de histórico;
- 8 comandos idempotentes.

## Aplicação

Primeiro, use o Flyway somente para criar o schema:

```powershell
$env:FLYWAY_ENABLED='true'
.\api-numerario\mvnw.cmd -f .\api-numerario\pom.xml spring-boot:run
```

Depois da primeira inicialização, volte `FLYWAY_ENABLED=false`. O antigo
seed pode então ser aplicado explicitamente:

```powershell
Get-Content .\database\scripts\seed-dados-dev.sql | mysql -u root -p gestao_numerario
```

Nunca aplique esse arquivo em homologação ou produção: ele contém credenciais
conhecidas e dados determinísticos de demonstração.
