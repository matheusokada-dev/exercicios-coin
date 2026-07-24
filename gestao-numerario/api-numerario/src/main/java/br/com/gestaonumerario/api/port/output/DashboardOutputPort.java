package br.com.gestaonumerario.api.port.output;

import br.com.gestaonumerario.api.core.domain.model.ResumoDashboard;

import java.time.LocalDate;

public interface DashboardOutputPort {
    ResumoDashboard consultar(LocalDate dataReferencia);
}

