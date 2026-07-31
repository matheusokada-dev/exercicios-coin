package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.model.UnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.command.AjustarDivergenciaCommand;
import br.com.gestaonumerario.api.core.domain.model.command.CargaInicialTesourariaCommand;

public interface FinanceiroNumerarioInputPort {
    UnidadeOperacional realizarCargaInicial(CargaInicialTesourariaCommand command);

    UnidadeOperacional ajustarDivergencia(AjustarDivergenciaCommand command);
}
