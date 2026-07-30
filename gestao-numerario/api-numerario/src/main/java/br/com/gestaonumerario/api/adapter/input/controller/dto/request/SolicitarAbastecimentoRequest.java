package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SolicitarAbastecimentoRequest(
        @NotNull Long agenciaId,
        @NotNull @DecimalMin(value = "0.01") @Digits(
                integer = 17,
                fraction = 2
        ) BigDecimal valor,
        @NotBlank @Size(max = 500) String motivo,
        @NotNull LocalDate dataDesejada
) {
}
