# Coin Cadastro de Produtos

Aplicacao full stack para cadastro, listagem, alteracao e exclusao logica de produtos.

O projeto possui:

- Backend de dominio em Spring Boot, responsavel por regras de produto, persistencia, validacoes e banco de dados.
- BFF em Spring Boot, responsavel por expor um contrato especifico para o frontend e integrar com o backend de produtos.
- Frontend em Angular, responsavel pela experiencia de uso.

Arquitetura atual:

```text
Angular :4200
  -> BFF Cadastro Produtos :8081
      -> Backend Cadastro Produtos :8080
          -> MySQL :3306
```

## Tecnologias

### Backend de Produtos

- Java 21
- Spring Boot 3.5.0
- Spring Web
- Spring Data JPA
- Bean Validation
- MySQL
- Flyway
- Lombok
- Springdoc OpenAPI / Swagger
- JUnit 5
- Mockito

### BFF

- Java 21
- Spring Boot 3.5.0
- Spring Web
- Bean Validation
- Spring Boot Actuator
- RestClient
- MapStruct
- Lombok
- Springdoc OpenAPI / Swagger
- JUnit 5
- Mockito

### Frontend

- Angular 19
- TypeScript
- Angular Router
- Angular Forms
- HttpClient
- RxJS

## Estrutura Do Projeto

```text
coin-cadastro-produtos/
  backend/
    cadastro-produtos/
      src/main/java/br/com/coin/cadastroprodutos/
        config/
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

    bff-cadastro-produtos/
      src/main/java/br/com/coin/bffcadastroprodutos/
        clients/
        config/
        controllers/
        dtos/
          backend/
          bff/
        enums/
        exceptions/
        handlers/
        mappers/
        services/
      src/main/resources/
        application.properties
      src/test/java/

  frontend/
    cadastro-produtos/
      src/app/
        components/
        interceptors/
        models/
        services/
```

## Funcionalidades

- Cadastrar produto com modal de confirmacao.
- Criar produto como ativo por padrao.
- Listar produtos ativos e inativos.
- Filtrar por nome, status e faixa de preco.
- Aplicar filtros somente ao clicar no botao `Filtrar`.
- Alterar quantidade de itens por pagina diretamente, mantendo os filtros ja aplicados.
- Ordenar por nome, codigo, preco e status.
- Paginar resultados com 5, 10, 20 ou 50 itens por pagina.
- Alterar produto a partir da listagem ou de busca.
- Alterar status do produto entre ativo e inativo.
- Confirmar alteracoes em modal com campos modificados.
- Excluir produto por exclusao logica.
- Exibir tela de erro para falhas de infraestrutura.
- Disponibilizar Swagger para backend e BFF.

Observacao: excluir produto nao remove o registro do banco. A operacao altera o campo `ativo` para `false`.

## Banco De Dados

O backend de produtos espera um banco MySQL local com o nome:

```sql
coin_cadastro_produtos
```

Crie o banco antes de iniciar o backend:

```sql
CREATE DATABASE coin_cadastro_produtos;
```

Configuracao atual:

```text
backend/cadastro-produtos/src/main/resources/application.properties
```

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

## Backend De Produtos

Base URL:

```text
http://localhost:8080/produtos
```

Endpoints:

```text
POST   /produtos
GET    /produtos
GET    /produtos/{id}
PUT    /produtos/{id}
DELETE /produtos/{id}
```

O backend e dono das regras de dominio:

- Persistencia com JPA.
- Validacoes de dados.
- Padronizacao final do nome do produto.
- Exclusao logica.
- Regras de produto inexistente ou ja desativado.

## BFF Cadastro Produtos

Base URL:

```text
http://localhost:8081/api/bff/produtos
```

Endpoints:

```text
POST   /api/bff/produtos
GET    /api/bff/produtos
GET    /api/bff/produtos/{id}
PUT    /api/bff/produtos/{id}
DELETE /api/bff/produtos/{id}
```

A BFF integra com o backend atual:

```text
BFF POST   /api/bff/produtos      -> Backend POST   /produtos
BFF GET    /api/bff/produtos      -> Backend GET    /produtos
BFF GET    /api/bff/produtos/{id} -> Backend GET    /produtos/{id}
BFF PUT    /api/bff/produtos/{id} -> Backend PUT    /produtos/{id}
BFF DELETE /api/bff/produtos/{id} -> Backend DELETE /produtos/{id}
```

Responsabilidades da BFF:

- Encapsular a URL do backend.
- Expor contrato orientado ao frontend.
- Validar filtros superficiais.
- Definir defaults de listagem.
- Converter erros de infraestrutura em respostas apropriadas.
- Preservar erros de negocio do backend quando aplicavel.

Tratamento de erros na BFF:

```text
Backend fora do ar       -> 503
Timeout                  -> 504
Erro 5xx do backend      -> 503
Erro 4xx do backend      -> preservado
Erro generico da BFF     -> 500
```

## Parametros De Listagem

Parametros aceitos na listagem:

| Parametro     | Exemplo    | Descricao                                  |
| ------------- | ---------- | ------------------------------------------ |
| `page`        | `0`        | Pagina solicitada. A primeira pagina e `0` |
| `size`        | `5`        | Quantidade de itens por pagina             |
| `sort`        | `nome,asc` | Campo e direcao da ordenacao               |
| `busca`       | `cabo`     | Busca por nome do produto                  |
| `status`      | `ativos`   | Aceita `todos`, `ativos` ou `inativos`     |
| `precoMinimo` | `10.00`    | Preco minimo, inclusivo                    |
| `precoMaximo` | `100.00`   | Preco maximo, inclusivo                    |

Exemplo:

```http
GET /api/bff/produtos?page=0&size=5&sort=nome,asc&status=todos&busca=cabo
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

## Fluxos Do Frontend

### Cadastro

1. Usuario preenche nome e preco.
2. Sistema mostra os dados para confirmacao.
3. Usuario confirma.
4. Frontend chama a BFF.
5. BFF chama o backend.
6. Backend padroniza nome, salva e retorna o produto.
7. Sistema exibe mensagem de sucesso ou erro.

### Listagem

1. Tela carrega automaticamente com 5 produtos por pagina.
2. Usuario altera busca, status, ordenacao ou faixa de preco.
3. Filtros so sao aplicados ao clicar em `Filtrar`.
4. Sistema valida busca e faixa de preco.
5. Sistema busca produtos pela BFF.
6. Usuario pode alterar a quantidade de itens por pagina sem clicar em `Filtrar`.
7. Ao mudar itens por pagina, os filtros ja aplicados sao preservados.
8. Acoes por linha permitem alterar ou excluir.
9. Produtos inativos continuam visiveis, mas nao podem ser excluidos novamente.

### Alteracao

1. Usuario busca um produto ou abre a alteracao pela listagem.
2. Formulario e preenchido automaticamente.
3. Usuario altera nome, preco ou status.
4. Modal mostra campos modificados com antes/depois.
5. Usuario confirma ou volta para editar.

### Exclusao

1. Usuario busca um produto ativo ou usa o botao da listagem.
2. Sistema mostra os dados do produto.
3. Modal informa que o produto sera marcado como inativo.
4. Usuario confirma.
5. Produto passa a aparecer como `Inativo`.

### Erro De Infraestrutura

Quando a BFF, o backend ou o banco ficam indisponiveis, o frontend:

1. Mantem loading por alguns segundos.
2. Redireciona para `/erro?tipo=infra`.
3. Exibe uma mensagem de erro de conexao.

Erros tratados pelo interceptor:

```text
status 0
status 503
status 504
```

## Padronizacao De Nomes

O backend padroniza o nome no cadastro e na alteracao.

Exemplos:

| Entrada         | Saida           |
| --------------- | --------------- |
| `aRRoZ`         | `Arroz`         |
| `arroz branco`  | `Arroz Branco`  |
| `cabo hdmi`     | `Cabo HDMI`     |
| `adaptador usb` | `Adaptador USB` |

Siglas conhecidas preservadas:

```text
HDMI, USB, LED, LCD, SSD, HD, CPU, GPU, RAM, TV, DVD, CD, VGA, RGB
```

## Validacoes

### Cadastro

- `nome` e obrigatorio.
- `preco` e obrigatorio.
- `preco` deve ser maior que zero.
- Produto nasce ativo por padrao.

### Alteracao

- `nome` e obrigatorio.
- `nome` deve ter no maximo 120 caracteres.
- `preco` e obrigatorio.
- `preco` deve ser maior que zero.
- `ativo` e obrigatorio.

### Listagem

- `busca` deve ter no maximo 120 caracteres no frontend.
- `precoMinimo` nao pode ser negativo.
- `precoMaximo` nao pode ser negativo.
- `precoMinimo` nao pode ser maior que `precoMaximo`.
- `size` permitido na BFF: `5`, `10`, `20`, `50`.
- `page` nao pode ser negativa.

## Formato De Erro

Formato usado pelas APIs:

```json
{
  "codError": 1000,
  "msgError": "Produto nao existente."
}
```

Exemplos de erros tratados:

- Produto inexistente.
- Produto ja desativado.
- Request invalido.
- Servico de produtos indisponivel.
- Timeout na integracao.
- Erro generico.

## Swagger / OpenAPI

A documentacao Swagger foi implementada usando interfaces separadas dos controllers.

Backend:

```text
ProdutoApi
ProdutoController implements ProdutoApi
```

BFF:

```text
ProdutoBffApi
ProdutoBffController implements ProdutoBffApi
```

Acessos:

```text
Backend:
http://localhost:8080/swagger-ui.html

BFF:
http://localhost:8081/swagger-ui.html
```

JSON OpenAPI:

```text
http://localhost:8080/v3/api-docs
http://localhost:8081/v3/api-docs
```

## Mappers

### Backend Atual

O backend usa `ProdutoMapper` como classe Spring:

```java
@Component
public class ProdutoMapper
```

Ele foi mantido assim porque contem regra customizada de padronizacao de nome.

### BFF

A BFF usa MapStruct:

```java
@Mapper(componentModel = "spring")
public interface ProdutoBffMapper
```

MapStruct gera os metodos simples de conversao entre DTOs. O metodo que converte pagina continua como `default`, pois possui logica manual para mapear o conteudo paginado.

## Como Executar

### 1. Subir MySQL

Garanta que o MySQL esta rodando e que o banco existe:

```sql
CREATE DATABASE coin_cadastro_produtos;
```

### 2. Subir Backend De Produtos

Windows / PowerShell:

```powershell
cd backend/cadastro-produtos
.\mvnw.cmd spring-boot:run
```

URL:

```text
http://localhost:8080
```

### 3. Subir BFF

Em outro terminal, a partir da raiz do projeto:

```powershell
.\backend\cadastro-produtos\mvnw.cmd -f backend\bff-cadastro-produtos\pom.xml spring-boot:run
```

URL:

```text
http://localhost:8081
```

### 4. Subir Frontend

```powershell
cd frontend/cadastro-produtos
npm install
npm start
```

URL:

```text
http://localhost:4200
```

## Testes

### Backend

```powershell
cd backend/cadastro-produtos
.\mvnw.cmd test
```

Cobertura atual:

```text
17 testes
```

### BFF

A BFF nao possui wrapper Maven proprio. Use o wrapper do backend:

```powershell
.\backend\cadastro-produtos\mvnw.cmd -f backend\bff-cadastro-produtos\pom.xml test
```

Cobertura atual:

```text
23 testes
```

### Frontend

Validacao TypeScript:

```powershell
cd frontend/cadastro-produtos
npx tsc -p tsconfig.app.json --noEmit
```

Observacao:

```text
npm test nao roda atualmente porque nao existem arquivos .spec.ts no frontend.
```

## Observacoes

- O frontend consome a BFF em `http://localhost:8081/api/bff/produtos`.
- A BFF consome o backend em `http://localhost:8080/produtos`.
- O backend usa `spring.jpa.hibernate.ddl-auto=validate`.
- A estrutura do banco precisa estar de acordo com as migrations do Flyway.
- Se o banco nao existir ou as credenciais estiverem incorretas, o backend nao inicia corretamente.
- Depois de alterar dependencias Maven, recarregue o projeto Maven na IDE.
- No PowerShell, use `.\mvnw.cmd`, nao apenas `mvnw.cmd`.
