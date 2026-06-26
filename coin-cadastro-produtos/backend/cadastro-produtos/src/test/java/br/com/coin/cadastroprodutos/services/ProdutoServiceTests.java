package br.com.coin.cadastroprodutos.services;

import br.com.coin.cadastroprodutos.dtos.ProdutoRequestDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoResponseDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDTO;
import br.com.coin.cadastroprodutos.entities.Produto;
import br.com.coin.cadastroprodutos.exceptions.ProdutoDesativadoException;
import br.com.coin.cadastroprodutos.exceptions.ProdutoNaoEncontradoException;
import br.com.coin.cadastroprodutos.mappers.ProdutoMapper;
import br.com.coin.cadastroprodutos.repositories.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoMapper produtoMapper;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    void deveCriarProdutoComSucesso() {
        ProdutoRequestDTO requestDTO = new ProdutoRequestDTO("Arroz", BigDecimal.valueOf(25.90));

        Produto produto = new Produto();
        produto.setNome("Arroz");
        produto.setPreco(new BigDecimal("25.90"));
        produto.setAtivo(true);

        Produto produtoSalvo = new Produto();
        produtoSalvo.setId(1L);
        produtoSalvo.setNome("Arroz");
        produtoSalvo.setPreco(new BigDecimal("25.90"));
        produtoSalvo.setAtivo(true);

        ProdutoResponseDTO responseDTO = new ProdutoResponseDTO(1L,
                                                            "Arroz",
                                                                BigDecimal.valueOf(25.90),
                                                                true);

        when(produtoMapper.toEntity(requestDTO)).thenReturn(produto);
        when(produtoRepository.save(produto)).thenReturn(produtoSalvo);
        when(produtoMapper.toResponseDTO(produtoSalvo)).thenReturn(responseDTO);

        ProdutoResponseDTO resultado = produtoService.criar(requestDTO);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Arroz", resultado.nome());
        assertEquals(new BigDecimal("25.9"), resultado.preco());
        assertTrue(resultado.ativo());

        verify(produtoMapper, times(1)).toEntity(requestDTO);
        verify(produtoRepository, times(1)).save(produto);
        verify(produtoMapper, times(1)).toResponseDTO(produtoSalvo);
    }

    @Test
    void deveListarProdutosAtivosComSucesso() {
        Produto produto = criarProduto(1L, "Arroz", "25.90", true);
        ProdutoResponseDTO responseDTO = new ProdutoResponseDTO(1L, "Arroz", new BigDecimal("25.90"), true);

        when(produtoRepository.findByAtivoTrue()).thenReturn(List.of(produto));
        when(produtoMapper.toResponseDTO(produto)).thenReturn(responseDTO);

        List<ProdutoResponseDTO> resultado = produtoService.listarAtivos();

        assertEquals(1, resultado.size());
        assertEquals(responseDTO, resultado.getFirst());

        verify(produtoRepository, times(1)).findByAtivoTrue();
        verify(produtoMapper, times(1)).toResponseDTO(produto);
    }

    @Test
    void deveListarProdutosInativosComSucesso() {
        Produto produto = criarProduto(1L, "Arroz", "25.90", false);
        ProdutoResponseDTO responseDTO = new ProdutoResponseDTO(1L, "Arroz", new BigDecimal("25.90"), false);

        when(produtoRepository.findByAtivoFalse()).thenReturn(List.of(produto));
        when(produtoMapper.toResponseDTO(produto)).thenReturn(responseDTO);

        List<ProdutoResponseDTO> resultado = produtoService.listarInativos();

        assertEquals(1, resultado.size());
        assertEquals(responseDTO, resultado.getFirst());

        verify(produtoRepository, times(1)).findByAtivoFalse();
        verify(produtoMapper, times(1)).toResponseDTO(produto);
    }

    @Test
    void deveBuscarProdutoPorIdComSucesso() {
        Produto produto = criarProduto(1L, "Arroz", "25.90", true);
        ProdutoResponseDTO responseDTO = new ProdutoResponseDTO(1L, "Arroz", new BigDecimal("25.90"), true);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoMapper.toResponseDTO(produto)).thenReturn(responseDTO);

        ProdutoResponseDTO resultado = produtoService.buscarPorId(1L);

        assertEquals(responseDTO, resultado);

        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoMapper, times(1)).toResponseDTO(produto);
    }

    @Test
    void deveLancarExcecaoAoBuscarProdutoInexistente() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProdutoNaoEncontradoException.class, () -> produtoService.buscarPorId(99L));

        verify(produtoRepository, times(1)).findById(99L);
        verifyNoInteractions(produtoMapper);
    }

    @Test
    void deveAtualizarProdutoComSucesso() {
        ProdutoUpdateDTO updateDTO = new ProdutoUpdateDTO("Feijao", new BigDecimal("12.50"), true);
        Produto produto = criarProduto(1L, "Arroz", "25.90", true);
        Produto produtoAtualizado = criarProduto(1L, "Feijao", "12.50", true);
        ProdutoResponseDTO responseDTO = new ProdutoResponseDTO(1L, "Feijao", new BigDecimal("12.50"), true);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produtoAtualizado);
        when(produtoMapper.toResponseDTO(produtoAtualizado)).thenReturn(responseDTO);

        ProdutoResponseDTO resultado = produtoService.atualizar(1L, updateDTO);

        assertEquals(responseDTO, resultado);

        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoMapper, times(1)).updateEntity(produto, updateDTO);
        verify(produtoRepository, times(1)).save(produto);
        verify(produtoMapper, times(1)).toResponseDTO(produtoAtualizado);
    }

    @Test
    void deveDesativarProdutoComSucesso() {
        Produto produto = criarProduto(1L, "Arroz", "25.90", true);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produto);

        produtoService.desativar(1L);

        assertEquals(false, produto.getAtivo());

        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(produto);
    }

    @Test
    void deveLancarExcecaoAoDesativarProdutoJaDesativado() {
        Produto produto = criarProduto(1L, "Arroz", "25.90", false);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        assertThrows(ProdutoDesativadoException.class, () -> produtoService.desativar(1L));

        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, never()).save(produto);
    }

    private Produto criarProduto(Long id, String nome, String preco, Boolean ativo) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome(nome);
        produto.setPreco(new BigDecimal(preco));
        produto.setAtivo(ativo);
        return produto;
    }
}
