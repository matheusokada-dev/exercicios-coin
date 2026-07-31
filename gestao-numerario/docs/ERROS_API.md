# Padrão de erros

API e BFF retornam erros em JSON e nunca expõem stack trace ou nomes internos de
classes. A API fornece o contrato mais completo:

```json
{
  "timestamp": "2026-07-25T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "CAMPO_INVALIDO",
  "message": "Um ou mais campos informados são inválidos.",
  "path": "/api/v1/solicitacoes-numerario",
  "fields": [
    {
      "field": "valor",
      "message": "O campo 'valor' deve respeitar o valor mínimo permitido."
    }
  ],
  "codError": 1006,
  "msgError": "Um ou mais campos informados são inválidos."
}
```

`codError` e `msgError` permanecem por compatibilidade. Novos consumidores
devem preferir `code`, `message` e `fields`.

## Categorias

| Faixa/código | HTTP | Uso |
| --- | --- | --- |
| `1` | 500 | Falha inesperada, sem detalhes internos |
| `1000` | 400 | Campo obrigatório ausente |
| `1001`–`1006` | 400 | Valor ou formato inválido |
| `2000`–`2004` | 403/409/422 | Autorização, saldo ou transição |
| `3000`–`3009` | 400/401/404/409 | Recursos, duplicidade e credenciais |
| `4000`–`4002` | 409/410/422 | Operações de numerário e versão |
| `9000`–`9001` | 503/504 | Indisponibilidade ou timeout no BFF |

## Regras para mensagens

- Identificar o campo quando o erro for de entrada.
- Informar a condição que impediu uma operação de negócio.
- Não registrar senhas, tokens, corpos completos ou dados pessoais.
- Não devolver mensagens de banco, nomes de tabelas, classes ou stack traces.
- Preservar o código estável ao tornar um texto mais claro.

## Exemplos

Campo ausente:

```json
{
  "code": "CAMPO_OBRIGATORIO",
  "message": "O campo 'agenciaId' é obrigatório.",
  "fields": [
    {
      "field": "agenciaId",
      "message": "O campo 'agenciaId' é obrigatório."
    }
  ]
}
```

Regra de negócio:

```json
{
  "code": "REGRA_OPERACAO_NUMERARIO_VIOLADA",
  "message": "A carga inicial da Tesouraria já foi realizada."
}
```

Concorrência:

```json
{
  "status": 409,
  "code": "CONFLITO_VERSAO",
  "message": "O registro foi alterado por outra operação."
}
```
