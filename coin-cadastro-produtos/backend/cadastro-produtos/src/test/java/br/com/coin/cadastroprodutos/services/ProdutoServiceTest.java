package br.com.coin.cadastroprodutos.services;

import br.com.coin.cadastroprodutos.dtos.ProdutoDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoDtoResponse;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoFiltroDtoRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        ProdutoDtoRequest requestDto = new ProdutoDtoRequest("Arroz", BigDecimal.valueOf(25.90));

        Produto produto = new Produto();
        produto.setNome("Arroz");
        produto.setPreco(new BigDecimal("25.90"));
        produto.setAtivo(true);

        Produto produtoSalvo = new Produto();
        produtoSalvo.setId(1L);
        produtoSalvo.setNome("Arroz");
        produtoSalvo.setPreco(new BigDecimal("25.90"));
        produtoSalvo.setAtivo(true);

        ProdutoDtoResponse responseDto = new ProdutoDtoResponse(1L,
                "Arroz",
                BigDecimal.valueOf(25.90),
                true);

        when(produtoMapper.toEntity(requestDto)).thenReturn(produto);
        when(produtoRepository.save(produto)).thenReturn(produtoSalvo);
        when(produtoMapper.toDtoResponse(produtoSalvo)).thenReturn(responseDto);

        ProdutoDtoResponse resultado = produtoService.criar(requestDto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Arroz", resultado.nome());
        assertEquals(new BigDecimal("25.9"), resultado.preco());
        assertTrue(resultado.ativo());

        verify(produtoMapper, times(1)).toEntity(requestDto);
        verify(produtoRepository, times(1)).save(produto);
        verify(produtoMapper, times(1)).toDtoResponse(produtoSalvo);
    }

    @Test
    void deveListarProdutosComFiltrosEPaginacaoComSucesso() {
        ProdutoFiltroDtoRequest filtro = new ProdutoFiltroDtoRequest("Arroz", "todos", null, null);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("nome").ascending());
        Produto produto = criarProduto(1L, "Arroz", "25.90", true);
        ProdutoDtoResponse responseDto = new ProdutoDtoResponse(1L, "Arroz", new BigDecimal("25.90"), true);
        Page<Produto> paginaProdutos = new PageImpl<>(List.of(produto), pageable, 1);

        when(produtoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(paginaProdutos);
        when(produtoMapper.toDtoResponse(produto)).thenReturn(responseDto);

        Page<ProdutoDtoResponse> resultado = produtoService.listar(filtro, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals(responseDto, resultado.getContent().getFirst());

        verify(produtoRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(produtoMapper, times(1)).toDtoResponse(produto);
    }

    @Test
    void deveBuscarProdutoPorIdComSucesso() {
        Produto produto = criarProduto(1L, "Arroz", "25.90", true);
        ProdutoDtoResponse responseDto = new ProdutoDtoResponse(1L, "Arroz", new BigDecimal("25.90"), true);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoMapper.toDtoResponse(produto)).thenReturn(responseDto);

        ProdutoDtoResponse resultado = produtoService.buscarPorId(1L);

        assertEquals(responseDto, resultado);

        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoMapper, times(1)).toDtoResponse(produto);
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
        ProdutoUpdateDtoRequest updateDto = new ProdutoUpdateDtoRequest("Feijao", new BigDecimal("12.50"), true);
        Produto produto = criarProduto(1L, "Arroz", "25.90", true);
        Produto produtoAtualizado = criarProduto(1L, "Feijao", "12.50", true);
        ProdutoDtoResponse responseDto = new ProdutoDtoResponse(1L, "Feijao", new BigDecimal("12.50"), true);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produtoAtualizado);
        when(produtoMapper.toDtoResponse(produtoAtualizado)).thenReturn(responseDto);

        ProdutoDtoResponse resultado = produtoService.atualizar(1L, updateDto);

        assertEquals(responseDto, resultado);

        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoMapper, times(1)).updateEntity(produto, updateDto);
        verify(produtoRepository, times(1)).save(produto);
        verify(produtoMapper, times(1)).toDtoResponse(produtoAtualizado);
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

