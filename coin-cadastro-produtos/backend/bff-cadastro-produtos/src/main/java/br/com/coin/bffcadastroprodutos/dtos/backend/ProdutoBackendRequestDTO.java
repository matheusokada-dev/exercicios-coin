package br.com.coin.bffcadastroprodutos.dtos.backend;

import java.math.BigDecimal;

public record ProdutoBackendRequestDTO(
        String nome,
        BigDecimal preco
) {
}