package br.com.gestaonumerario.bff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record OcorrenciaOperacaoNumerarioRequest(
        @NotBlank @Size(max = 500) String descricao,
        @NotNull @PositiveOrZero Integer versaoOperacao
) {
}
