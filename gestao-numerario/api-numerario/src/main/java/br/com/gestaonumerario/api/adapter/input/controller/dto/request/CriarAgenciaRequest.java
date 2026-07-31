package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CriarAgenciaRequest(
        @NotBlank
        @Size(max = 10)
        @Pattern(
                regexp = "\\d+",
                message = "O código da agência deve conter somente números."
        )
        String codigo,
        @NotBlank @Size(max = 120) String nome,
        @NotBlank
        @Size(max = 100)
        @Pattern(
                regexp = "^[\\p{L} ]+$",
                message = "A cidade deve conter somente letras e espaços."
        )
        String cidade,
        @NotNull
        @PositiveOrZero
        @DecimalMax(
                value = "99999999999999999.00",
                message = "O saldo atual deve ser de no máximo R$ 99.999.999.999.999.999,00."
        )
        @Digits(
                integer = 17,
                fraction = 2
        ) BigDecimal saldoAtual,
        @NotNull
        @PositiveOrZero
        @DecimalMax(
                value = "99999999999999999.00",
                message = "O limite mínimo deve ser de no máximo R$ 99.999.999.999.999.999,00."
        )
        @Digits(
                integer = 17,
                fraction = 2
        ) BigDecimal limiteMinimo
) {
}
