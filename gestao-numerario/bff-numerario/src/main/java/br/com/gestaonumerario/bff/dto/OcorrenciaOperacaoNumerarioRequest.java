package br.com.gestaonumerario.bff.dto;
import jakarta.validation.constraints.*;
public record OcorrenciaOperacaoNumerarioRequest(
 @NotBlank @Size(max=500) String descricao,@NotNull @PositiveOrZero Integer versaoOperacao) {}
