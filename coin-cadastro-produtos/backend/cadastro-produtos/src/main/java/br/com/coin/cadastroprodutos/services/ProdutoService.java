package br.com.coin.cadastroprodutos.services;


import br.com.coin.cadastroprodutos.dtos.FiltroProdutoDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoRequestDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoResponseDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDTO;
import br.com.coin.cadastroprodutos.entities.Produto;
import br.com.coin.cadastroprodutos.exceptions.ProdutoDesativadoException;
import br.com.coin.cadastroprodutos.exceptions.ProdutoNaoEncontradoException;
import br.com.coin.cadastroprodutos.mappers.ProdutoMapper;
import br.com.coin.cadastroprodutos.repositories.ProdutoRepository;
import br.com.coin.cadastroprodutos.specifications.ProdutoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    @Transactional
    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        Produto produto = produtoMapper.toEntity(dto);
        Produto produtoSalvo = produtoRepository.save(produto);
        return produtoMapper.toResponseDTO(produtoSalvo);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listar(FiltroProdutoDTO filtro, Pageable pageable) {
        return produtoRepository
                .findAll(ProdutoSpecification.comFiltros(filtro), pageable)
                .map(produtoMapper::toResponseDTO);
    }


    @Transactional
    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = buscarProdutoPorId(id);
        return produtoMapper.toResponseDTO(produto);
    }

    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoUpdateDTO dto) {
        Produto produto = buscarProdutoPorId(id);
        produtoMapper.updateEntity(produto, dto);
        Produto produtoAtualizado = produtoRepository.save(produto);
        return produtoMapper.toResponseDTO(produtoAtualizado);
    }

    @Transactional
    public void desativar(Long id) {
        Produto produto = buscarProdutoPorId(id);
        verificarSeProdutoDesativado(produto);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    private Produto buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(ProdutoNaoEncontradoException::new);
    }

    private void verificarSeProdutoDesativado (Produto produto){
        if (!produto.getAtivo()) {
            throw new ProdutoDesativadoException();
        }
    }
}
