package br.com.coin.bffcadastroprodutos.handlers;

import br.com.coin.bffcadastroprodutos.exceptions.BackendIndisponivelException;
import br.com.coin.bffcadastroprodutos.exceptions.BackendResponseException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseExceptionHandlerTest {

    private final BaseExceptionHandler handler = new BaseExceptionHandler();

    @Test
    void deveResponderErroControladoDaBff() {
        var response = handler.handleBffException(new BackendIndisponivelException());

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(503, response.getBody().codError());
        assertEquals("Serviço de produtos indisponível.", response.getBody().msgError());
    }

    @Test
    void devePreservarErroControladoDoBackend() {
        var exception = new BackendResponseException(
                HttpStatus.NOT_FOUND,
                1000,
                "Produto não existente."
        );

        var response = handler.handleBackendResponseException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(1000, response.getBody().codError());
        assertEquals("Produto não existente.", response.getBody().msgError());
    }
}
