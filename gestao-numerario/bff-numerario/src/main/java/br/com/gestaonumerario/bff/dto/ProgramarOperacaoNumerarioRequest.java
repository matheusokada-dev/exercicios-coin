package br.com.gestaonumerario.bff.dto;
import jakarta.validation.constraints.*;
public record ProgramarOperacaoNumerarioRequest(
 @NotNull @Positive Long unidadeFaltanteId,@NotNull @PositiveOrZero Integer versaoSolicitacao) {}
