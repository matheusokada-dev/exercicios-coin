package br.com.gestaonumerario.bff.dto;
import jakarta.validation.constraints.*;
public record DecidirSolicitacaoNumerarioRequest(
 @NotBlank @Size(max=500) String justificativa,@NotNull @PositiveOrZero Integer versao) {}
