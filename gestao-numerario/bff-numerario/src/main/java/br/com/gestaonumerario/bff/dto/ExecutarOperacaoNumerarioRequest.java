package br.com.gestaonumerario.bff.dto;
import jakarta.validation.constraints.*;
public record ExecutarOperacaoNumerarioRequest(
 @NotNull @PositiveOrZero Integer versaoOperacao,@NotNull @PositiveOrZero Integer versaoUnidade) {}
