package br.com.coin.cadastroprodutos.controllers;

import br.com.coin.cadastroprodutos.dtos.ProdutoRequestDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoResponseDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDTO;
import br.com.coin.cadastroprodutos.services.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        ProdutoRequestDTO requestDTO = new ProdutoRequestDTO("Arroz", BigDecimal.valueOf(10.00));
        ProdutoResponseDTO responseDTO = new ProdutoResponseDTO(1L,"Arroz", BigDecimal.valueOf(10.00),true);
        when(produtoService.criar(requestDTO)).thenReturn(responseDTO);

        // Act
        ResponseEntity<ProdutoResponseDTO> response = produtoController.criar(requestDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        verify(produtoService, times(1)).criar(requestDTO);
    }

    @Test
    void testListarProdutosAtivos() {
        // Arrange
        List<ProdutoResponseDTO> produtos = new ArrayList<>();
        when(produtoService.listarAtivos()).thenReturn(produtos);

        // Act
        ResponseEntity<List<ProdutoResponseDTO>> response = produtoController.listarAtivos();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(produtos, response.getBody());
        verify(produtoService, times(1)).listarAtivos();
    }

    @Test
    void testListarProdutosInativos() {
        // Arrange
        List<ProdutoResponseDTO> produtos = new ArrayList<>();
        when(produtoService.listarInativos()).thenReturn(produtos);

        // Act
        ResponseEntity<List<ProdutoResponseDTO>> response = produtoController.listarInativos();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(produtos, response.getBody());
        verify(produtoService, times(1)).listarInativos();
    }

    @Test
    void testBuscarProdutoPorId() {
        // Arrange
        Long produtoId = 1L;
        ProdutoResponseDTO responseDTO = new ProdutoResponseDTO(1L,"Arroz", BigDecimal.valueOf(10.00),true);
        when(produtoService.buscarPorId(produtoId)).thenReturn(responseDTO);

        // Act
        ResponseEntity<ProdutoResponseDTO> response = produtoController.buscarPorId(produtoId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        verify(produtoService, times(1)).buscarPorId(produtoId);
    }

    @Test
    void testAtualizarProduto() {
        // Arrange
        Long produtoId = 1L;
        ProdutoUpdateDTO updateDTO = new ProdutoUpdateDTO("Arroz", BigDecimal.valueOf(10.00),true);
        ProdutoResponseDTO responseDTO = new ProdutoResponseDTO(1L,"Arroz", BigDecimal.valueOf(10.00),true);
        when(produtoService.atualizar(produtoId, updateDTO)).thenReturn(responseDTO);

        // Act
        ResponseEntity<ProdutoResponseDTO> response = produtoController.atualizar(produtoId, updateDTO);

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