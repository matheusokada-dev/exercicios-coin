package br.com.coin.bffcadastroprodutos.dtos.bff;

import java.math.BigDecimal;

public record ProdutoBffResponseDTO(
        Long id,
        String nome,
        BigDecimal preco,
        Boolean ativo
) {
}