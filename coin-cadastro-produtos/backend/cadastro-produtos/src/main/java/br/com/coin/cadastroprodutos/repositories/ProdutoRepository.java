package br.com.coin.cadastroprodutos.repositories;
import br.com.coin.cadastroprodutos.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrue();
    List<Produto> findByAtivoFalse();
}