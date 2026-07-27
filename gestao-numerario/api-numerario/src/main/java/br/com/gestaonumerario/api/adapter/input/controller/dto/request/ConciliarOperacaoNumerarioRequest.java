package br.com.gestaonumerario.api.adapter.input.controller.dto.request;
import jakarta.validation.constraints.*;
public record ConciliarOperacaoNumerarioRequest(
        @NotBlank @Size(max=500) String justificativa,
        @NotNull @PositiveOrZero Long versaoOperacao) {}
