package br.com.gestaonumerario.api.adapter.input.controller.dto.request;
import jakarta.validation.constraints.*;
public record ExecutarOperacaoNumerarioRequest(
        @NotNull @PositiveOrZero Long versaoOperacao,
        @NotNull @PositiveOrZero Long versaoUnidade) {}
