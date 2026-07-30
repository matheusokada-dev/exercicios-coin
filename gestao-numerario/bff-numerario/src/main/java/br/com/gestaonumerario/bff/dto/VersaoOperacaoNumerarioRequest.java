package br.com.gestaonumerario.bff.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record VersaoOperacaoNumerarioRequest(@NotNull @PositiveOrZero Integer versaoOperacao) {
}
