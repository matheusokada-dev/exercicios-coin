package br.com.coin.bffcadastroprodutos.dtos.backend;

import java.math.BigDecimal;

public record ProdutoBackendDtoRequest(
        String nome,
        BigDecimal preco
) {
}