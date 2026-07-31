package br.com.gestaonumerario.bff.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record GerarLivroCaixaRequest(
        @NotNull @Positive Long agenciaId,
        @NotNull @PastOrPresent LocalDate dataInicio,
        @NotNull @PastOrPresent LocalDate dataFim
) {
}
