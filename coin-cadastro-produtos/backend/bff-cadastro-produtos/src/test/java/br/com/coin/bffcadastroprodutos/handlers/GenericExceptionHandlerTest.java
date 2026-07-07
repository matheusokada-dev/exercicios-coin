package br.com.coin.bffcadastroprodutos.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenericExceptionHandlerTest {

    private final GenericExceptionHandler handler = new GenericExceptionHandler();

    @Test
    void deveResponderErroGenerico() {
        var response = handler.handleException(new RuntimeException("falha inesperada"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(1, response.getBody().codError());
        assertEquals("Erro interno na BFF.", response.getBody().msgError());
    }
}
