package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import br.com.gestaonumerario.api.core.domain.enums.TipoOperacaoNumerario;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarSolicitacaoNumerarioRequest(
        @NotNull TipoOperacaoNumerario tipoOperacao,
        @NotNull @Positive Long agenciaId,
        @NotNull @DecimalMin(value = "0.01") @Digits(
                integer = 17,
                fraction = 2
        ) BigDecimal valor,
        @NotBlank @Size(max = 500) String motivo,
        @NotNull @FutureOrPresent LocalDate dataDesejada
) {
}
