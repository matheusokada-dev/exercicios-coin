package br.com.gestaonumerario.bff.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ReceberOperacaoNumerarioRequest(
        @NotNull @DecimalMin("0.01") @Digits(
                integer = 17,
                fraction = 2
        ) BigDecimal valorRecebido,
        @Size(max = 500) String justificativaDivergencia,
        @NotNull @PositiveOrZero Integer versaoOperacao,
        @NotNull @PositiveOrZero Integer versaoUnidade
) {
}
