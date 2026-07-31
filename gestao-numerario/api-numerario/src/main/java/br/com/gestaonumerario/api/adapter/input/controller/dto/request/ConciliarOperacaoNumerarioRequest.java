package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ConciliarOperacaoNumerarioRequest(
        @NotBlank @Size(max = 500) String justificativa,
        @NotNull @PositiveOrZero Long versaoOperacao
) {
}
