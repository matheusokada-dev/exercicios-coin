package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ExecutarOperacaoNumerarioRequest(
        @NotNull @PositiveOrZero Long versaoOperacao,
        @NotNull @PositiveOrZero Long versaoUnidade
) {
}
