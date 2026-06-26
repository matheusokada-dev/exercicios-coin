package br.com.coin.cadastroprodutos.dtos;

import java.math.BigDecimal;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        BigDecimal preco,
        Boolean ativo
) {}