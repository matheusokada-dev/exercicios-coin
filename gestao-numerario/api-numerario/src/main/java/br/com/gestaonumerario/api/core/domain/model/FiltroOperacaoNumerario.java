package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.StatusOperacaoNumerario;
import java.time.LocalDate;

public record FiltroOperacaoNumerario(
        StatusOperacaoNumerario status, Long origemId, Long destinoId,
        LocalDate dataInicio, LocalDate dataFim, int pagina, int tamanho) {
}
