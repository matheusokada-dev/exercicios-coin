package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProgramarOperacaoNumerarioRequest(
        @NotNull @Positive Long unidadeFaltanteId,
        @NotNull @PositiveOrZero Long versaoSolicitacao
) {
}
