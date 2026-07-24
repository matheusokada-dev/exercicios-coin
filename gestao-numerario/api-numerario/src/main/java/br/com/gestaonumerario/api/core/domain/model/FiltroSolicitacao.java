package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacao;
import br.com.gestaonumerario.api.core.exception.PeriodoConsultaInvalidoException;

import java.time.LocalDate;

public record FiltroSolicitacao(Long agenciaId, StatusSolicitacao status, LocalDate dataInicio,
                                LocalDate dataFim, int pagina, int tamanho) {
    public FiltroSolicitacao {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new PeriodoConsultaInvalidoException();
        }
    }
}
