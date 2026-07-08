package br.com.coin.cadastroprodutos.controllers;

import br.com.coin.cadastroprodutos.dtos.ProdutoFiltroDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoDtoResponse;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDtoRequest;
import br.com.coin.cadastroprodutos.services.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProdutoControllerTest {

    @InjectMocks
    private ProdutoController produtoController;

    @Mock
    private ProdutoService produtoService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCriarProduto() {
        // Arrange
        ProdutoDtoRequest requestDTO = new ProdutoDtoRequest("Arroz", BigDecimal.valueOf(10.00));
        ProdutoDtoResponse responseDTO = new ProdutoDtoResponse(1L,"Arroz", BigDecimal.valueOf(10.00),true);
        when(produtoService.criar(requestDTO)).thenReturn(responseDTO);

        // Act
        ResponseEntity<ProdutoDtoResponse> response = produtoController.criar(requestDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        verify(produtoService, times(1)).criar(requestDTO);
    }

    @Test
    void testListarProdutos() {
        // Arrange
        ProdutoFiltroDtoRequest filtro = new ProdutoFiltroDtoRequest("Arroz", "todos", null, null);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("nome").ascending());
        ProdutoDtoResponse produto = new ProdutoDtoResponse(1L, "Arroz", BigDecimal.valueOf(10.00), true);
        Page<ProdutoDtoResponse> produtos = new PageImpl<>(List.of(produto), pageable, 1);
        when(produtoService.listar(filtro, pageable)).thenReturn(produtos);

        // Act
        Page<ProdutoDtoResponse> response = produtoController.listar(filtro, pageable);

        // Assert
        assertEquals(produtos, response);
        assertEquals(1, response.getTotalElements());
        verify(produtoService, times(1)).listar(filtro, pageable);
    }

    @Test
    void testBuscarProdutoPorId() {
        // Arrange
        Long produtoId = 1L;
        ProdutoDtoResponse responseDTO = new ProdutoDtoResponse(1L,"Arroz", BigDecimal.valueOf(10.00),true);
        when(produtoService.buscarPorId(produtoId)).thenReturn(responseDTO);

        // Act
        ResponseEntity<ProdutoDtoResponse> response = produtoController.buscarPorId(produtoId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        verify(produtoService, times(1)).buscarPorId(produtoId);
    }

    @Test
    void testAtualizarProduto() {
        // Arrange
        Long produtoId = 1L;
        ProdutoUpdateDtoRequest updateDTO = new ProdutoUpdateDtoRequest("Arroz", BigDecimal.valueOf(10.00),true);
        ProdutoDtoResponse responseDTO = new ProdutoDtoResponse(1L,"Arroz", BigDecimal.valueOf(10.00),true);
        when(produtoService.atualizar(produtoId, updateDTO)).thenReturn(responseDTO);

        // Act
        ResponseEntity<ProdutoDtoResponse> response = produtoController.atualizar(produtoId, updateDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        verify(produtoService, times(1)).atualizar(produtoId, updateDTO);
    }

    @Test
    void testDesativarProduto() {
        // Arrange
        Long produtoId = 1L;
        doNothing().when(produtoService).desativar(produtoId);

        // Act
        ResponseEntity<Void> response = produtoController.desativar(produtoId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(produtoService, times(1)).desativar(produtoId);
    }
}
