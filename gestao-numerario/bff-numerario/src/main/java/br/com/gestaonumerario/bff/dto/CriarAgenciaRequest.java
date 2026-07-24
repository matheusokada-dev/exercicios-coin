package br.com.gestaonumerario.bff.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CriarAgenciaRequest(
        @NotBlank @Size(max = 10) String codigo,
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 100) String cidade,
        @NotNull @Digits(integer = 17, fraction = 2) BigDecimal saldoAtual,
        @NotNull @Digits(integer = 17, fraction = 2) BigDecimal limiteMinimo
) {
}
