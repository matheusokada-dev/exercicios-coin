package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import br.com.gestaonumerario.api.core.domain.enums.TipoOperacaoNumerario;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarSolicitacaoNumerarioRequest(
        @NotNull TipoOperacaoNumerario tipoOperacao,
        @NotNull @Positive Long agenciaId,
        @NotNull @DecimalMin(value="0.01") @Digits(integer=17, fraction=2) BigDecimal valor,
        @NotBlank @Size(max=500) String motivo,
        @NotNull @FutureOrPresent LocalDate dataDesejada) {}
