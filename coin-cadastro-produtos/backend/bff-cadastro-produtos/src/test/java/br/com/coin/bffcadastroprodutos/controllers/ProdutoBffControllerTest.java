package br.com.coin.bffcadastroprodutos.controllers;

import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffFiltroDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.response.ProdutoBffPageDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.response.ProdutoBffDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffUpdateDtoRequest;
import br.com.coin.bffcadastroprodutos.services.ProdutoBffService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoBffControllerTest {

    @Mock
    private ProdutoBffService produtoBffService;

    @InjectMocks
    private ProdutoBffController produtoBffController;

    @Test
    void deveCriarProdutoComStatusCreated() {
        var request = new ProdutoBffDtoRequest("Mouse", new BigDecimal("59.90"));
        var response = new ProdutoBffDtoResponse(1L, "Mouse", new BigDecimal("59.90"), true);

        when(produtoBffService.criar(request)).thenReturn(response);

        var resultado = produtoBffController.criar(request);

        assertEquals(HttpStatus.CREATED, resultado.getStatusCode());
        assertEquals(response, resultado.getBody());
        verify(produtoBffService).criar(request);
    }

    @Test
    void deveListarProdutosComStatusOk() {
        var filtro = new ProdutoBffFiltroDtoRequest(0, 5, "id,asc", null, "todos", null, null);
        var produto = new ProdutoBffDtoResponse(1L, "Mouse", new BigDecimal("59.90"), true);
        var page = new ProdutoBffPageDtoResponse<>(List.of(produto), 1L, 1, 5, 0);

        when(produtoBffService.listar(filtro)).thenReturn(page);

        var resultado = produtoBffController.listar(filtro);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(page, resultado.getBody());
        verify(produtoBffService).listar(filtro);
    }

    @Test
    void deveBuscarProdutoPorIdComStatusOk() {
        var response = new ProdutoBffDtoResponse(1L, "Mouse", new BigDecimal("59.90"), true);

        when(produtoBffService.buscarPorId(1L)).thenReturn(response);

        var resultado = produtoBffController.buscarPorId(1L);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(response, resultado.getBody());
        verify(produtoBffService).buscarPorId(1L);
    }

    @Test
    void deveAtualizarProdutoComStatusOk() {
        var update = new ProdutoBffUpdateDtoRequest("Mouse Gamer", new BigDecimal("99.90"), true);
        var response = new ProdutoBffDtoResponse(1L, "Mouse Gamer", new BigDecimal("99.90"), true);

        when(produtoBffService.atualizar(1L, update)).thenReturn(response);

        var resultado = produtoBffController.atualizar(1L, update);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(response, resultado.getBody());
        verify(produtoBffService).atualizar(1L, update);
    }

    @Test
    void deveDesativarProdutoComStatusNoContent() {
        var resultado = produtoBffController.desativar(1L);

        assertEquals(HttpStatus.NO_CONTENT, resultado.getStatusCode());
        verify(produtoBffService).desativar(1L);
    }
}
