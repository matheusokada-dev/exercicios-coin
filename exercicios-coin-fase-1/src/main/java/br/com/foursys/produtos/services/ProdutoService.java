package br.com.foursys.produtos.services;

import br.com.foursys.produtos.models.Produto;
import br.com.foursys.produtos.exceptions.ProdutoNaoEncontradoException;
import br.com.foursys.produtos.interfaces.RepositorioProduto;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class ProdutoService {
    private final RepositorioProduto repositorioProduto;

    public Produto criarProduto(Long id, String nome, BigDecimal preco) {
        Produto produto = new Produto(id, nome, preco, true);
        return repositorioProduto.salvar(produto);
    }

    public List<Produto> listarProdutos() {
        return repositorioProduto.listarTodos();
    }

    public List<Produto> filtrarPorPreco(BigDecimal precoMaximo) {
        return repositorioProduto.listarTodos()
                .stream()
                .filter(produto -> produto.getPreco().compareTo(precoMaximo) <= 0)
                .toList();
    }

    public List<Produto> ordenarPorNome() {
        return repositorioProduto.listarTodos()
                .stream()
                .sorted(Comparator.comparing(Produto::getNome))
                .toList();
    }

    public Produto buscarPorId(Long id) {
        return repositorioProduto.buscarPorId(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não existe"));
    }

    public void deletarProduto(Long id) {
        buscarPorId(id);
        repositorioProduto.deletar(id);
    }


    public void deletarSoftProduto(Long id) {
        buscarPorId(id);
        repositorioProduto.deletarSoft(id);
    }
}
