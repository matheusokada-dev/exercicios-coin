package br.com.gestaonumerario.bff.dto;
import jakarta.validation.constraints.*;
public record ConciliarOperacaoNumerarioRequest(
 @NotBlank @Size(max=500) String justificativa,@NotNull @PositiveOrZero Integer versaoOperacao) {}
