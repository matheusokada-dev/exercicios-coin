package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.exception.PeriodoConsultaInvalidoException;
import java.time.LocalDate;

public record FiltroMovimentacao(
        Long agenciaId,
        TipoMovimentacao tipo,
        LocalDate dataInicio,
        LocalDate dataFim,
        int pagina,
        int tamanho
) {
    public FiltroMovimentacao {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new PeriodoConsultaInvalidoException();
        }
    }
}
