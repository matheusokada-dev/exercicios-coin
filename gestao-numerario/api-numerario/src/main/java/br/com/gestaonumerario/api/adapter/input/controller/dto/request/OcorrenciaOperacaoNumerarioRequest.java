package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record OcorrenciaOperacaoNumerarioRequest(
        @NotBlank @Size(max = 500) String descricao,
        @NotNull @PositiveOrZero Long versaoOperacao
) {
}
