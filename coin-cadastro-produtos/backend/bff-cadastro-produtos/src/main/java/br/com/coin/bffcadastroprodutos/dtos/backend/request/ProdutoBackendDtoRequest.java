package br.com.coin.bffcadastroprodutos.dtos.backend.request;

import java.math.BigDecimal;

public record ProdutoBackendDtoRequest(
        String nome,
        BigDecimal preco
) {
}