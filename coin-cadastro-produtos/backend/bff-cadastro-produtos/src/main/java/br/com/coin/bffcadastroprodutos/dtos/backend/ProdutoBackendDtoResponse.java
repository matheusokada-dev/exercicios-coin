package br.com.coin.bffcadastroprodutos.dtos.backend;

import java.math.BigDecimal;

public record ProdutoBackendDtoResponse(
        Long id,
        String nome,
        BigDecimal preco,
        Boolean ativo
) {
}