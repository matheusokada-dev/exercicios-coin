package br.com.gestaonumerario.api.adapter.input.controller.dto.request;
import jakarta.validation.constraints.*;
public record ProgramarOperacaoNumerarioRequest(
        @NotNull @Positive Long unidadeFaltanteId,
        @NotNull @PositiveOrZero Long versaoSolicitacao) {}
