package br.com.foursys.produtos.interfaces;

import br.com.foursys.produtos.models.Produto;

import java.util.List;
import java.util.Optional;

public interface RepositorioProduto {

    Produto salvar(Produto produto);

    List<Produto> listarTodos();

    Optional<Produto> buscarPorId(Long id);

    void deletar(Long id);

    void deletarSoft(Long id);
}
