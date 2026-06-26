package br.com.coin.cadastroprodutos.mappers;


import br.com.coin.cadastroprodutos.dtos.ProdutoRequestDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoResponseDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDTO;
import br.com.coin.cadastroprodutos.entities.Produto;
import org.springframework.stereotype.Component;

@Component
public class ProdutoMapper {

    public Produto toEntity(ProdutoRequestDTO dto) {
        Produto produto = new Produto();
        produto.setNome(dto.nome());
        produto.setPreco(dto.preco());
        produto.setAtivo(true);
        return produto;
    }

    public ProdutoResponseDTO toResponseDTO(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getPreco(),
                produto.getAtivo()
        );
    }

    public void updateEntity(Produto produto, ProdutoUpdateDTO dto) {
        produto.setNome(dto.nome());
        produto.setPreco(dto.preco());
        produto.setAtivo(dto.ativo());
    }
}