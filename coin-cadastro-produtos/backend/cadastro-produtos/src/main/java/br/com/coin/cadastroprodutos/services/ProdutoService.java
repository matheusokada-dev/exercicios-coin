package br.com.coin.cadastroprodutos.services;


import br.com.coin.cadastroprodutos.dtos.ProdutoFiltroDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoDtoResponse;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDtoRequest;
import br.com.coin.cadastroprodutos.entities.Produto;
import br.com.coin.cadastroprodutos.exceptions.ProdutoDesativadoException;
import br.com.coin.cadastroprodutos.exceptions.ProdutoNaoEncontradoException;
import br.com.coin.cadastroprodutos.mappers.ProdutoMapper;
import br.com.coin.cadastroprodutos.repositories.ProdutoRepository;
import br.com.coin.cadastroprodutos.specifications.ProdutoSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    @Transactional
    public ProdutoDtoResponse criar(ProdutoDtoRequest dto) {
        Produto produto = produtoMapper.toEntity(dto);
        Produto produtoSalvo = produtoRepository.save(produto);
        log.info("Produto criado com id={}", produtoSalvo.getId());
        return produtoMapper.toDtoResponse(produtoSalvo);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoDtoResponse> listar(ProdutoFiltroDtoRequest filtro, Pageable pageable) {
        Page<ProdutoDtoResponse> produtos = produtoRepository
                .findAll(ProdutoSpecification.comFiltros(filtro), pageable)
                .map(produtoMapper::toDtoResponse);
        log.info("Listagem de produtos retornou {} registros de um total de {}",
                produtos.getNumberOfElements(), produtos.getTotalElements());
        return produtos;
    }


    @Transactional
    public ProdutoDtoResponse buscarPorId(Long id) {
        Produto produto = buscarProdutoPorId(id);
        return produtoMapper.toDtoResponse(produto);
    }

    @Transactional
    public ProdutoDtoResponse atualizar(Long id, ProdutoUpdateDtoRequest dto) {
        Produto produto = buscarProdutoPorId(id);
        produtoMapper.updateEntity(produto, dto);
        Produto produtoAtualizado = produtoRepository.save(produto);
        log.info("Produto atualizado com id={}", produtoAtualizado.getId());
        return produtoMapper.toDtoResponse(produtoAtualizado);
    }

    @Transactional
    public void desativar(Long id) {
        Produto produto = buscarProdutoPorId(id);
        verificarSeProdutoDesativado(produto);
        produto.setAtivo(false);
        produtoRepository.save(produto);
        log.info("Produto desativado com id={}", id);
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
