package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoOperacaoNumerario;
import java.time.LocalDate;

public record FiltroSolicitacaoNumerario(
        Long agenciaId, TipoOperacaoNumerario tipo, StatusSolicitacaoNumerario status,
        Long origemId, Long destinoId, LocalDate dataInicio, LocalDate dataFim,
        int pagina, int tamanho) {
}
