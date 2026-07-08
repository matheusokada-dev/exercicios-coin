package br.com.coin.bffcadastroprodutos.dtos.frontend;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record ProdutoBffFiltroDtoRequest(
        Integer page,
        Integer size,
        String sort,
        String busca,
        String status,

        @DecimalMin(value = "0.00", message = "Preço mínimo não pode ser negativo")
        BigDecimal precoMinimo,

        @DecimalMin(value = "0.00", message = "Preço máximo não pode ser negativo")
        BigDecimal precoMaximo
) {
}