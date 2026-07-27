package br.com.gestaonumerario.api.adapter.input.controller.dto.request;
import jakarta.validation.constraints.*;
public record VersaoOperacaoNumerarioRequest(
        @NotNull @PositiveOrZero Long versaoOperacao) {}
