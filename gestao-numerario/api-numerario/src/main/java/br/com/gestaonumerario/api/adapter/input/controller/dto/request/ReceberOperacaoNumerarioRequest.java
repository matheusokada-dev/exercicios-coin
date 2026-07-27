package br.com.gestaonumerario.api.adapter.input.controller.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ReceberOperacaoNumerarioRequest(
        @NotNull @DecimalMin("0.01") @Digits(integer=17, fraction=2) BigDecimal valorRecebido,
        @Size(max=500) String justificativaDivergencia,
        @NotNull @PositiveOrZero Long versaoOperacao,
        @NotNull @PositiveOrZero Long versaoUnidade) {}
