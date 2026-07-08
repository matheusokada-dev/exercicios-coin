package br.com.coin.bffcadastroprodutos.services;

import br.com.coin.bffcadastroprodutos.clients.ProdutoBackendClient;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendPageDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendUpdateDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffFiltroDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffUpdateDtoRequest;
import br.com.coin.bffcadastroprodutos.exceptions.ProdutoBffValidationException;
import br.com.coin.bffcadastroprodutos.mappers.ProdutoBffMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoBffServiceTest {

    @Mock
    private ProdutoBackendClient produtoBackendClient;

    @Mock
    private ProdutoBffMapper produtoBffMapper;

    @InjectMocks
    private ProdutoBffService produtoBffService;

    @Test
    void deveCriarProdutoComSucesso() {
        var request = new ProdutoBffDtoRequest("Mouse", new BigDecimal("59.90"));
        var backendRequest = new ProdutoBackendDtoRequest("Mouse", new BigDecimal("59.90"));
        var backendResponse = new ProdutoBackendDtoResponse(1L, "Mouse", new BigDecimal("59.90"), true);
        var bffResponse = new ProdutoBffDtoResponse(1L, "Mouse", new BigDecimal("59.90"), true);

        when(produtoBffMapper.toBackendRequest(request)).thenReturn(backendRequest);
        when(produtoBackendClient.criar(backendRequest)).thenReturn(backendResponse);
        when(produtoBffMapper.toBffResponse(backendResponse)).thenReturn(bffResponse);

        var resultado = produtoBffService.criar(request);

        assertEquals(bffResponse, resultado);
        verify(produtoBffMapper).toBackendRequest(request);
        verify(produtoBackendClient).criar(backendRequest);
        verify(produtoBffMapper).toBffResponse(backendResponse);
    }

    @Test
    void deveListarUsandoValoresPadraoQuandoFiltroNaoInformar() {
        var filtro = new ProdutoBffFiltroDtoRequest(null, null, null, null, null, null, null);
        var backendProduto = new ProdutoBackendDtoResponse(1L, "Mouse", new BigDecimal("59.90"), true);
        var backendPage = new ProdutoBackendPageDtoResponse<>(List.of(backendProduto), 1L, 1, 5, 0);
        var bffPage = new br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffPageDtoResponse<>(
                List.of(new ProdutoBffDtoResponse(1L, "Mouse", new BigDecimal("59.90"), true)),
                1L,
                1,
                5,
                0
        );

        when(produtoBackendClient.listar(0, 5, "id,asc", null, "todos", null, null)).thenReturn(backendPage);
        when(produtoBffMapper.toBffPage(backendPage)).thenReturn(bffPage);

        var resultado = produtoBffService.listar(filtro);

        assertEquals(bffPage, resultado);
        verify(produtoBackendClient).listar(0, 5, "id,asc", null, "todos", null, null);
        verify(produtoBffMapper).toBffPage(backendPage);
    }

    @Test
    void deveLancarExcecaoQuandoPaginaForNegativa() {
        var filtro = new ProdutoBffFiltroDtoRequest(-1, 5, "id,asc", null, "todos", null, null);

        assertThrows(ProdutoBffValidationException.class, () -> produtoBffService.listar(filtro));
    }

    @Test
    void deveLancarExcecaoQuandoTamanhoPaginaForInvalido() {
        var filtro = new ProdutoBffFiltroDtoRequest(0, 7, "id,asc", null, "todos", null, null);

        assertThrows(ProdutoBffValidationException.class, () -> produtoBffService.listar(filtro));
    }

    @Test
    void deveLancarExcecaoQuandoPrecoMinimoForMaiorQuePrecoMaximo() {
        var filtro = new ProdutoBffFiltroDtoRequest(
                0,
                5,
                "id,asc",
                null,
                "todos",
                new BigDecimal("100.00"),
                new BigDecimal("10.00")
        );

        assertThrows(ProdutoBffValidationException.class, () -> produtoBffService.listar(filtro));
    }

    @Test
    void deveAtualizarProdutoComSucesso() {
        var update = new ProdutoBffUpdateDtoRequest("Mouse Gamer", new BigDecimal("99.90"), true);
        var backendUpdate = new ProdutoBackendUpdateDtoRequest("Mouse Gamer", new BigDecimal("99.90"), true);
        var backendResponse = new ProdutoBackendDtoResponse(1L, "Mouse Gamer", new BigDecimal("99.90"), true);
        var bffResponse = new ProdutoBffDtoResponse(1L, "Mouse Gamer", new BigDecimal("99.90"), true);

        when(produtoBffMapper.toBackendUpdate(update)).thenReturn(backendUpdate);
        when(produtoBackendClient.atualizar(1L, backendUpdate)).thenReturn(backendResponse);
        when(produtoBffMapper.toBffResponse(backendResponse)).thenReturn(bffResponse);

        var resultado = produtoBffService.atualizar(1L, update);

        assertEquals(bffResponse, resultado);
        verify(produtoBackendClient).atualizar(1L, backendUpdate);
    }

    @Test
    void deveDesativarProdutoComSucesso() {
        produtoBffService.desativar(1L);

        verify(produtoBackendClient).desativar(1L);
    }
}
