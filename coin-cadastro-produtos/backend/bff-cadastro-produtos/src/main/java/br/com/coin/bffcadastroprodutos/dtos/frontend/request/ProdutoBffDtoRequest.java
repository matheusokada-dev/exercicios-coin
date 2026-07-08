package br.com.coin.bffcadastroprodutos.dtos.frontend.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProdutoBffDtoRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        BigDecimal preco
) {
}