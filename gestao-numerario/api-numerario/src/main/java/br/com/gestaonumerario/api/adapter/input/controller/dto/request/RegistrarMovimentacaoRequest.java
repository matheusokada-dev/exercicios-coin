package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RegistrarMovimentacaoRequest(
        @NotNull Long agenciaId,
        @NotNull TipoMovimentacao tipo,
        Boolean entradaAjuste,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal valor,
        @NotBlank @Size(max = 500) String descricao,
        @NotBlank @Size(max = 80) String idempotencyKey
) {
}

