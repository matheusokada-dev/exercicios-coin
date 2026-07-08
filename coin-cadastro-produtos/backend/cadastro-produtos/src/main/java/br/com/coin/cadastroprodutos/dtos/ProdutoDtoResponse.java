package br.com.coin.cadastroprodutos.dtos;

import java.math.BigDecimal;

public record ProdutoDtoResponse(
        Long id,
        String nome,
        BigDecimal preco,
        Boolean ativo
) {}