package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AjustarDivergenciaRequest(
        @NotNull @Positive Long unidadeId,
        @NotNull @DecimalMin(value = "0.01") @Digits(
                integer = 17,
                fraction = 2
        ) BigDecimal valor,
        boolean entrada,
        @NotBlank @Size(max = 500) String justificativa,
        @NotNull @PositiveOrZero Long versaoUnidade
) {
}
