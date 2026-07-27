package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CargaInicialTesourariaRequest(
        @NotNull @DecimalMin(value="0.01") @Digits(integer=17, fraction=2) BigDecimal valor,
        @NotBlank @Size(max=500) String justificativa,
        @NotNull @PositiveOrZero Long versaoUnidade
) {}
