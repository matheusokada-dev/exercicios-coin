# Coin Cadastro de Produtos

Aplicacao full stack para cadastro, listagem, alteracao e desativacao de produtos.

O projeto possui um backend em Spring Boot e um frontend em Angular. 
O backend expoe uma API REST para produtos, usa MySQL como banco de dados e Flyway para criar/popular a tabela inicial. 
O frontend consome essa API e oferece telas para cadastrar, alterar, deletar/desativar e listar produtos.

## Tecnologias

### Backend

- Java 21
- Spring Boot 3.5.0
- Spring Web
- Spring Data JPA
- Bean Validation
- MySQL
- Flyway
- Lombok
- JUnit 5
- Mockito
- MockMvc

### Frontend

- Angular 19
- TypeScript
- Angular Router
- Angular Forms
- HttpClient

## Estrutura do Projeto

```text
coin-cadastro-produtos/
  backend/
    cadastro-produtos/
      src/main/java/br/com/coin/cadastroprodutos/
        controllers/
        dtos/
        entities/
        enums/
        exceptions/
        handlers/
        mappers/
        repositories/
        services/
      src/main/resources/
        application.properties
        db/migration/
      src/test/java/
  frontend/
    cadastro-produtos/
      src/app/
        models/
        pages/
        services/
```

## Funcionalidades

- Cadastrar produto
- Listar produtos ativos
- Listar produtos inativos
- Buscar produto por ID
- Alterar produto
- Desativar produto

Observacao: a operacao de deletar nao remove o produto do banco. Ela faz uma exclusao logica, alterando o campo `ativo` para `false`.

## Banco de Dados

O projeto espera um banco MySQL local com o nome:

```sql
coin_cadastro_produtos
```

Crie o banco antes de iniciar o backend:

```sql
CREATE DATABASE coin_cadastro_produtos;
```

As configuracoes atuais ficam em:

```text
backend/cadastro-produtos/src/main/resources/application.properties
```

Configuracao usada pelo projeto:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/coin_cadastro_produtos
spring.datasource.username=root
spring.datasource.password=aluno1234
server.port=8080
```

Ao iniciar o backend, o Flyway executa as migrations em:

```text
backend/cadastro-produtos/src/main/resources/db/migration
```

Migrations atuais:

- `V1__criar_tabela_produtos.sql`: cria a tabela `produtos`
- `V2__insert_produtos.sql`: insere produtos iniciais

## Endpoints da API

Base URL:

```text
http://localhost:8080/produtos
```

### Criar Produto

```http
POST /produtos
```

Body:

```json
{
  "nome": "Mouse gamer",
  "preco": 89.90
}
```

Resposta esperada:

```json
{
  "id": 1,
  "nome": "Mouse gamer",
  "preco": 89.90,
  "ativo": true
}
```

### Listar Produtos Ativos

```http
GET /produtos
```

### Listar Produtos Inativos

```http
GET /produtos/inativos
```

### Buscar Produto por ID

```http
GET /produtos/{id}
```

### Alterar Produto

```http
PUT /produtos/{id}
```

Body:

```json
{
  "nome": "Mouse gamer atualizado",
  "preco": 99.90,
  "ativo": true
}
```

### Desativar Produto

```http
DELETE /produtos/{id}
```

Resposta esperada:

```text
204 No Content
```

## Validacoes

No cadastro:

- `nome` e obrigatorio
- `preco` e obrigatorio
- `preco` deve ser maior que zero

Na alteracao:

- `nome` e obrigatorio
- `nome` deve ter no maximo 120 caracteres
- `preco` e obrigatorio
- `preco` deve ser maior que zero
- `ativo` e obrigatorio

## Formato de Erro

Quando ocorre erro, a API retorna um objeto no formato:

```json
{
  "codError": 1000,
  "msgError": "Produto nao existente."
}
```

Exemplos de erros tratados:

- Produto inexistente
- Produto ja desativado
- Request invalido
- Erro generico

## Testes

O backend possui testes para service e controller.

## Observacoes

- O frontend consome a API em `http://localhost:8080/produtos`.
- O backend libera CORS para `http://localhost:4200`.
- O backend usa `spring.jpa.hibernate.ddl-auto=validate`, entao a estrutura do banco precisa estar de acordo com as migrations do Flyway.
- Se o banco nao existir ou as credenciais estiverem incorretas, o backend nao inicia corretamente.
