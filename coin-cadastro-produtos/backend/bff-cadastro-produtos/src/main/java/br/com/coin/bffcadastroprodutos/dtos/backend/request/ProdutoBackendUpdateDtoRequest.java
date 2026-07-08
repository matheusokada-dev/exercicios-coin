package br.com.coin.bffcadastroprodutos.dtos.backend;

import java.math.BigDecimal;

public record ProdutoBackendUpdateDtoRequest(
        String nome,
        BigDecimal preco,
        Boolean ativo
) {
}