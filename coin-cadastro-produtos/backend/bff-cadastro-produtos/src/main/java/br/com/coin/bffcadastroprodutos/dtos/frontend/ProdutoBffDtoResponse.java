package br.com.coin.bffcadastroprodutos.dtos.frontend;

import java.math.BigDecimal;

public record ProdutoBffDtoResponse(
        Long id,
        String nome,
        BigDecimal preco,
        Boolean ativo
) {
}