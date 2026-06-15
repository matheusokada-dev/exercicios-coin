package br.com.foursys.produtos.repositories;

import br.com.foursys.produtos.exceptions.ProdutoNaoEncontradoException;
import br.com.foursys.produtos.interfaces.RepositorioProduto;
import br.com.foursys.produtos.models.Produto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositoryEmMemoria implements RepositorioProduto {

    private final List<Produto> produtos = new ArrayList<>();

    @Override
    public Produto salvar(Produto produto) {
        produtos.add(produto);
        return produto;
    }

    @Override
    public List<Produto> listarTodos() {
        return produtos
                .stream()
                .filter(Produto::isAtivo)
                .toList();
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        for (Produto produto: produtos){
            if (produto.getId().equals(id)){
                return Optional.of(produto);
            }
        }
        return Optional.empty();
    }

    @Override
    public void deletar(Long id) {
        Produto produto = buscarPorId(id).orElseThrow(
                () -> new ProdutoNaoEncontradoException("Produto não existe"));
        produtos.remove(produto);
    }

    @Override
    public void deletarSoft(Long id){
        Produto produto = buscarPorId(id).orElseThrow(
                () -> new ProdutoNaoEncontradoException("Produto não existe"));
        produto.setAtivo(false);
    }


}
