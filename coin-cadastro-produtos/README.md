# Coin Cadastro de Produtos

Aplicacao full stack para cadastro, listagem, alteracao e exclusao logica de produtos.

O projeto possui um backend em Spring Boot e um frontend em Angular. O backend expoe uma API REST para produtos, usa MySQL como banco de dados e Flyway para criar/popular a tabela inicial. O frontend consome essa API e oferece fluxos com busca, filtros, paginacao, ordenacao e modais de confirmacao.

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

- Cadastrar produto com modal de confirmacao.
- Criar produto como ativo por padrao.
- Listar todos os produtos, ativos e inativos.
- Filtrar por nome, status e faixa de preco.
- Ordenar por nome, codigo, preco e status.
- Paginar resultados com 5, 10, 20 ou 50 itens por pagina.
- Alterar produto a partir de busca ou da listagem.
- Alterar status do produto com switch Ativo/Inativo.
- Confirmar alteracoes em modal com campos modificados e dados finais.
- Excluir produto com modal de confirmacao.
- Manter produto excluido como inativo, sem remove-lo do banco.
- Padronizar nomes de produtos, preservando siglas conhecidas como HDMI e USB.

Observacao: a operacao de excluir nao remove o produto do banco. Ela faz uma exclusao logica, alterando o campo `ativo` para `false`.

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

Parametros aceitos:

| Parametro | Exemplo | Descricao |
|---|---:|---|
| `page` | `0` | Pagina solicitada. A primeira pagina e `0`. |
| `size` | `5` | Quantidade de itens por pagina. |
| `sort` | `nome,asc` | Campo e direcao da ordenacao. |
| `busca` | `cabo` | Busca por nome do produto. |
| `status` | `ativos` | Aceita `todos`, `ativos` ou `inativos`. |
| `precoMinimo` | `10.00` | Preco minimo, inclusivo. |
| `precoMaximo` | `100.00` | Preco maximo, inclusivo. |

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

Ordenacoes usadas pelo frontend:

- `nome,asc`
- `nome,desc`
- `id,asc`
- `id,desc`
- `preco,asc`
- `preco,desc`
- `ativo,desc`
- `ativo,asc`

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

Observacao: o endpoint marca o produto como inativo. Se o produto ja estiver inativo, a API retorna erro tratado.

## Fluxos do Frontend

### Cadastro

1. Usuario preenche nome e preco.
2. Sistema padroniza o nome para revisao.
3. Modal mostra nome, preco e status inicial `Ativo`.
4. Usuario confirma o cadastro.
5. Sistema exibe mensagem de sucesso ou erro.

### Listagem

1. Tela carrega automaticamente com 5 produtos por pagina.
2. Usuario pode buscar por nome.
3. Usuario pode filtrar por status e faixa de preco.
4. Usuario pode ordenar por nome, codigo, preco ou status.
5. Qualquer filtro, ordenacao ou mudanca de tamanho volta para a pagina 1.
6. Acoes por linha permitem alterar ou excluir.
7. Produtos inativos continuam visiveis, mas nao podem ser excluidos novamente.

### Alteracao

1. Usuario busca um produto pelo nome ou abre a alteracao pela listagem.
2. Formulario e preenchido automaticamente.
3. Usuario altera nome, preco ou status.
4. Modal mostra os campos modificados com antes/depois.
5. Modal tambem mostra todos os dados finais.
6. Usuario confirma ou volta para editar.

### Exclusao

1. Usuario busca um produto ativo ou usa o botao da listagem.
2. Sistema mostra os dados do produto.
3. Modal informa que o produto sera marcado como inativo.
4. Usuario confirma.
5. Produto passa a aparecer como `Inativo`.

## Padronizacao de Nomes

O backend padroniza o nome no cadastro e na alteracao.

Exemplos:

| Entrada | Saida |
|---|---|
| `aRRoZ` | `Arroz` |
| `arroz branco` | `Arroz Branco` |
| `cabo hdmi` | `Cabo HDMI` |
| `adaptador usb` | `Adaptador USB` |

Siglas conhecidas preservadas:

```text
HDMI, USB, LED, LCD, SSD, HD, CPU, GPU, RAM, TV, DVD, CD, VGA, RGB
```

## Validacoes

No cadastro:

- `nome` e obrigatorio
- `preco` e obrigatorio
- `preco` deve ser maior que zero
- produto nasce ativo por padrao

Na alteracao:

- `nome` e obrigatorio
- `nome` deve ter no maximo 120 caracteres
- `preco` e obrigatorio
- `preco` deve ser maior que zero
- `ativo` e obrigatorio

Na listagem:

- `precoMinimo` nao pode ser negativo
- `precoMaximo` nao pode ser negativo
- no frontend, `precoMinimo` nao pode ser maior que `precoMaximo`

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

Por padrao, o Angular roda em:

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

## Observacoes

- O frontend consome a API em `http://localhost:8080/produtos`.
- O backend libera CORS para `http://localhost:4200`.
- O backend usa `spring.jpa.hibernate.ddl-auto=validate`, entao a estrutura do banco precisa estar de acordo com as migrations do Flyway.
- Se o banco nao existir ou as credenciais estiverem incorretas, o backend nao inicia corretamente.
- Depois de alterar regras no backend, reinicie a aplicacao para garantir que a versao nova esteja em execucao.
