package br.com.coin.bffcadastroprodutos.dtos.backend.request;

import java.math.BigDecimal;

public record ProdutoBackendUpdateDtoRequest(
        String nome,
        BigDecimal preco,
        Boolean ativo
) {
}