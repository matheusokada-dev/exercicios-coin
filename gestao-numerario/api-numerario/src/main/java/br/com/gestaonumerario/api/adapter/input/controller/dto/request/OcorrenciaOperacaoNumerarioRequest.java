package br.com.gestaonumerario.api.adapter.input.controller.dto.request;
import jakarta.validation.constraints.*;
public record OcorrenciaOperacaoNumerarioRequest(
        @NotBlank @Size(max=500) String descricao,
        @NotNull @PositiveOrZero Long versaoOperacao) {}
