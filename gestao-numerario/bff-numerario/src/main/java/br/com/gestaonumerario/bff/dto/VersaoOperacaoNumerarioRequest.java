package br.com.gestaonumerario.bff.dto;
import jakarta.validation.constraints.*;
public record VersaoOperacaoNumerarioRequest(@NotNull @PositiveOrZero Integer versaoOperacao) {}
