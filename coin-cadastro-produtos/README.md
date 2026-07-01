# Coin Cadastro de Produtos

Aplicação full stack para cadastro, listagem, alteração e exclusão lógica de produtos.

O projeto possui um backend em Spring Boot e um frontend em Angular. O backend expõe uma API REST para produtos, usa MySQL como banco de dados e Flyway para criar/popular a tabela inicial. O frontend consome essa API e oferece fluxos com busca, filtros, paginação, ordenação e modais de confirmação.

## Tecnologias

### Backend

* Java 21
* Spring Boot 3.5.0
* Spring Web
* Spring Data JPA
* Bean Validation
* MySQL
* Flyway
* Lombok
* JUnit 5
* Mockito

### Frontend

* Angular 19
* TypeScript
* Angular Router
* Angular Forms
* HttpClient

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
        specifications/
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

* Cadastrar produto com modal de confirmação.
* Criar produto como ativo por padrão.
* Listar todos os produtos, ativos e inativos.
* Filtrar por nome, status e faixa de preço.
* Ordenar por nome, código, preço e status.
* Paginar resultados com 5, 10, 20 ou 50 itens por página.
* Alterar produto a partir de busca ou da listagem.
* Alterar status do produto com switch Ativo/Inativo.
* Confirmar alterações em modal com campos modificados e dados finais.
* Excluir produto com modal de confirmação.
* Manter produto excluído como inativo, sem removê-lo do banco.

Observação: a operação de excluir não remove o produto do banco. Ela faz uma exclusão lógica, alterando o campo `ativo` para `false`.

## Banco de Dados

O projeto espera um banco MySQL local com o nome:

```sql
coin_cadastro_produtos
```

Crie o banco antes de iniciar o backend:

```sql
CREATE DATABASE coin_cadastro_produtos;
```

As configurações atuais ficam em:

```text
backend/cadastro-produtos/src/main/resources/application.properties
```

Configuração usada pelo projeto:

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

* `V1__criar_tabela_produtos.sql`: cria a tabela `produtos`
* `V2__insert_produtos.sql`: insere produtos iniciais

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
  "nome": "cabo hdmi",
  "preco": 89.90
}
```

Resposta esperada:

```json
{
  "id": 1,
  "nome": "Cabo HDMI",
  "preco": 89.90,
  "ativo": true
}
```

### Listar Produtos

```http
GET /produtos
```

Retorna produtos ativos e inativos em formato paginado.

Parâmetros aceitos:

| Parâmetro     |    Exemplo | Descrição                                   |
| ------------- | ---------: | ------------------------------------------- |
| `page`        |        `0` | Página solicitada. A primeira página é `0`. |
| `size`        |        `5` | Quantidade de itens por página.             |
| `sort`        | `nome,asc` | Campo e direção da ordenação.               |
| `busca`       |     `cabo` | Busca por nome do produto.                  |
| `status`      |   `ativos` | Aceita `todos`, `ativos` ou `inativos`.     |
| `precoMinimo` |    `10.00` | Preço mínimo, inclusivo.                    |
| `precoMaximo` |   `100.00` | Preço máximo, inclusivo.                    |

Exemplo:

```http
GET /produtos?page=0&size=5&sort=nome,asc&status=todos&busca=cabo
```

Resposta esperada:

```json
{
  "content": [
    {
      "id": 1,
      "nome": "Cabo HDMI",
      "preco": 89.90,
      "ativo": true
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 5,
  "number": 0
}
```

Ordenações usadas pelo frontend:

* `nome,asc`
* `nome,desc`
* `id,asc`
* `id,desc`
* `preco,asc`
* `preco,desc`
* `ativo,desc`
* `ativo,asc`

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
  "nome": "cabo hdmi atualizado",
  "preco": 99.90,
  "ativo": true
}
```

Resposta esperada:

```json
{
  "id": 1,
  "nome": "Cabo HDMI Atualizado",
  "preco": 99.90,
  "ativo": true
}
```

### Excluir Produto

```http
DELETE /produtos/{id}
```

Resposta esperada:

```text
204 No Content
```

Observação: o endpoint marca o produto como inativo. Se o produto já estiver inativo, a API retorna erro tratado.

## Fluxos do Frontend

### Cadastro

1. O usuário preenche nome e preço.
2. O sistema padroniza o nome para revisão.
3. O modal mostra nome, preço e status inicial `Ativo`.
4. O usuário confirma o cadastro.
5. O sistema exibe mensagem de sucesso ou erro.

### Listagem

1. A tela carrega automaticamente com 5 produtos por página.
2. O usuário pode buscar por nome.
3. O usuário pode filtrar por status e faixa de preço.
4. O usuário pode ordenar por nome, código, preço ou status.
5. Qualquer filtro, ordenação ou mudança de tamanho volta para a página 1.
6. Ações por linha permitem alterar ou excluir.
7. Produtos inativos continuam visíveis, mas não podem ser excluídos novamente.

### Alteração

1. O usuário busca um produto pelo nome ou abre a alteração pela listagem.
2. O formulário é preenchido automaticamente.
3. O usuário altera nome, preço ou status.
4. O modal mostra os campos modificados com antes/depois.
5. O modal também mostra todos os dados finais.
6. O usuário confirma ou volta para editar.

### Exclusão

1. O usuário busca um produto ativo ou usa o botão da listagem.
2. O sistema mostra os dados do produto.
3. O modal informa que o produto será marcado como inativo.
4. O usuário confirma.
5. O produto passa a aparecer como `Inativo`.

## Padronização de Nomes

O backend padroniza o nome no cadastro e na alteração.

Exemplos:

| Entrada         | Saída           |
| --------------- | --------------- |
| `aRRoZ`         | `Arroz`         |
| `arroz branco`  | `Arroz Branco`  |
| `cabo hdmi`     | `Cabo HDMI`     |
| `adaptador usb` | `Adaptador USB` |

Siglas conhecidas preservadas:

```text
HDMI, USB, LED, LCD, SSD, HD, CPU, GPU, RAM, TV, DVD, CD, VGA, RGB
```

## Validações

No cadastro:

* `nome` é obrigatório.
* `preco` é obrigatório.
* `preco` deve ser maior que zero.
* O produto nasce ativo por padrão.

Na alteração:

* `nome` é obrigatório.
* `nome` deve ter no máximo 120 caracteres.
* `preco` é obrigatório.
* `preco` deve ser maior que zero.
* `ativo` é obrigatório.

Na listagem:

* `precoMinimo` não pode ser negativo.
* `precoMaximo` não pode ser negativo.
* No frontend, `precoMinimo` não pode ser maior que `precoMaximo`.

## Formato de Erro

Quando ocorre erro, a API retorna um objeto no formato:

```json
{
  "codError": 1000,
  "msgError": "Produto não existente."
}
```

Exemplos de erros tratados:

* Produto inexistente.
* Produto já desativado.
* Request inválido.
* Erro genérico.

## Como Executar

### Backend

```bash
cd backend/cadastro-produtos
./mvnw spring-boot:run
```

No Windows:

```bash
cd backend/cadastro-produtos
mvnw.cmd spring-boot:run
```

### Frontend

```bash
cd frontend/cadastro-produtos
npm install
npm start
```

Por padrão, o Angular roda em:

```text
http://localhost:4200
```

## Testes

O backend possui testes para service e controller.

```bash
cd backend/cadastro-produtos
./mvnw test
```

No Windows:

```bash
cd backend/cadastro-produtos
mvnw.cmd test
```

## Observações

* O frontend consome a API em `http://localhost:8080/produtos`.
* O backend libera CORS para `http://localhost:4200`.
* O backend usa `spring.jpa.hibernate.ddl-auto=validate`, então a estrutura do banco precisa estar de acordo com as migrations do Flyway.
* Se o banco não existir ou as credenciais estiverem incorretas, o backend não inicia corretamente.
* Depois de alterar regras no backend, reinicie a aplicação para garantir que a versão nova esteja em execução.
