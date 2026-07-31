package br.com.gestaonumerario.bff.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ExecutarOperacaoNumerarioRequest(
        @NotNull @PositiveOrZero Integer versaoOperacao,
        @NotNull @PositiveOrZero Integer versaoUnidade
) {
}
