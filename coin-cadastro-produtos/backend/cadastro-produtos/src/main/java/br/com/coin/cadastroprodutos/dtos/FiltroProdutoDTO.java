package br.com.coin.cadastroprodutos.dtos;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record FiltroProdutoDTO(
        String busca,
        String status,

        @DecimalMin(value = "0.00", message = "Preco minimo nao pode ser negativo")
        BigDecimal precoMinimo,

        @DecimalMin(value = "0.00", message = "Preco maximo nao pode ser negativo")
        BigDecimal precoMaximo
) {
}
