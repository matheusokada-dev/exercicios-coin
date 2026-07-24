package br.com.coin.bffcadastroprodutos.clients;

import br.com.coin.bffcadastroprodutos.dtos.backend.request.ProdutoBackendDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.backend.request.ProdutoBackendUpdateDtoRequest;
import br.com.coin.bffcadastroprodutos.exceptions.BackendIndisponivelException;
import br.com.coin.bffcadastroprodutos.exceptions.BackendResponseException;
import br.com.coin.bffcadastroprodutos.exceptions.BackendTimeoutException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ProdutoBackendClientTest {

    private MockRestServiceServer server;
    private ProdutoBackendClient client;

    @BeforeEach
    void setup() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://backend-produtos");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ProdutoBackendClient(builder.build(), new ObjectMapper());
    }

    @Test
    void deveCriarProdutoChamandoBackend() {
        String responseBody = """
                {
                  "id": 1,
                  "nome": "Mouse",
                  "preco": 59.90,
                  "ativo": true
                }
                """;

        server.expect(requestTo("http://backend-produtos/produtos"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        var resultado = client.criar(new ProdutoBackendDtoRequest("Mouse", new BigDecimal("59.90")));

        assertEquals(1L, resultado.id());
        assertEquals("Mouse", resultado.nome());
        assertEquals(new BigDecimal("59.90"), resultado.preco());
        assertEquals(true, resultado.ativo());
        server.verify();
    }

    @Test
    void deveListarProdutosComQueryParams() {
        String responseBody = """
                {
                  "content": [
                    {
                      "id": 1,
                      "nome": "Mouse",
                      "preco": 59.90,
                      "ativo": true
                    }
                  ],
                  "totalElements": 1,
                  "totalPages": 1,
                  "size": 5,
                  "number": 0
                }
                """;

        server.expect(requestTo(containsString("http://backend-produtos/produtos")))
                .andExpect(requestTo(containsString("page=0")))
                .andExpect(requestTo(containsString("size=5")))
                .andExpect(requestTo(containsString("sort=id,asc")))
                .andExpect(requestTo(containsString("busca=mouse")))
                .andExpect(requestTo(containsString("status=todos")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        var resultado = client.listar(
                0,
                5,
                "id,asc",
                "mouse",
                "todos",
                null,
                null
        );

        assertEquals(1L, resultado.totalElements());
        assertEquals("Mouse", resultado.content().getFirst().nome());
        server.verify();
    }

    @Test
    void deveListarProdutosComFaixaDePreco() {
        String responseBody = """
                {
                  "content": [],
                  "totalElements": 0,
                  "totalPages": 0,
                  "size": 10,
                  "number": 0
                }
                """;

        server.expect(requestTo(containsString("http://backend-produtos/produtos")))
                .andExpect(requestTo(containsString("precoMinimo=10.00")))
                .andExpect(requestTo(containsString("precoMaximo=100.00")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        var resultado = client.listar(
                0,
                10,
                "preco,asc",
                null,
                "ativos",
                new BigDecimal("10.00"),
                new BigDecimal("100.00")
        );

        assertEquals(0L, resultado.totalElements());
        server.verify();
    }

    @Test
    void deveAtualizarProdutoChamandoBackend() {
        String responseBody = """
                {
                  "id": 1,
                  "nome": "Mouse Gamer",
                  "preco": 99.90,
                  "ativo": true
                }
                """;

        server.expect(requestTo("http://backend-produtos/produtos/1"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        var resultado = client.atualizar(
                1L,
                new ProdutoBackendUpdateDtoRequest("Mouse Gamer", new BigDecimal("99.90"), true)
        );

        assertEquals(1L, resultado.id());
        assertEquals("Mouse Gamer", resultado.nome());
        server.verify();
    }

    @Test
    void deveDesativarProdutoChamandoBackend() {
        server.expect(requestTo("http://backend-produtos/produtos/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        client.desativar(1L);

        server.verify();
    }

    @Test
    void deveConverterErro5xxDoBackendEmIndisponibilidade() {
        server.expect(requestTo("http://backend-produtos/produtos/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThrows(BackendIndisponivelException.class, () -> client.buscarPorId(1L));
        server.verify();
    }

    @Test
    void devePreservarErro4xxDoBackend() {
        String responseBody = """
                {
                  "codError": 1000,
                  "msgError": "Produto não existente."
                }
                """;

        server.expect(requestTo("http://backend-produtos/produtos/99"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody));

        var exception = assertThrows(BackendResponseException.class, () -> client.buscarPorId(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals(1000, exception.getCodError());
        assertEquals("Produto não existente.", exception.getMessage());
        server.verify();
    }

    @Test
    void deveUsarErroPadraoQuandoBackendRetornarJsonInvalido() {
        server.expect(requestTo("http://backend-produtos/produtos/99"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{invalid json"));

        var exception = assertThrows(BackendResponseException.class, () -> client.buscarPorId(99L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(400, exception.getCodError());
        assertEquals("Erro ao consultar serviço de produtos.", exception.getMessage());
        server.verify();
    }

    @Test
    void deveUsarErroPadraoQuandoBackendRetornarBodyVazio() {
        server.expect(requestTo("http://backend-produtos/produtos/99"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        var exception = assertThrows(BackendResponseException.class, () -> client.buscarPorId(99L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(400, exception.getCodError());
        assertEquals("Erro ao consultar serviço de produtos.", exception.getMessage());
        server.verify();
    }

    @Test
    void deveConverterTimeoutEmExcecaoEspecifica() {
        server.expect(requestTo("http://backend-produtos/produtos/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    throw new ResourceAccessException("timeout", new SocketTimeoutException("read timed out"));
                });

        assertThrows(BackendTimeoutException.class, () -> client.buscarPorId(1L));
        server.verify();
    }

    @Test
    void deveConverterFalhaDeConexaoEmIndisponibilidade() {
        server.expect(requestTo("http://backend-produtos/produtos/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    throw new ResourceAccessException("connection refused");
                });

        assertThrows(BackendIndisponivelException.class, () -> client.buscarPorId(1L));
        server.verify();
    }
}
