package br.com.gestaonumerario.bff.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProgramarOperacaoNumerarioRequest(
        @NotNull @Positive Long unidadeFaltanteId,
        @NotNull @PositiveOrZero Integer versaoSolicitacao
) {
}
