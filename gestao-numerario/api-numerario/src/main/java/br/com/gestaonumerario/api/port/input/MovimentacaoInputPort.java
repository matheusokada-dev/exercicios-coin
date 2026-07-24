package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.model.FiltroMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.Movimentacao;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.command.RegistrarMovimentacaoCommand;

public interface MovimentacaoInputPort {
    Movimentacao registrar(RegistrarMovimentacaoCommand command);
    Pagina<Movimentacao> consultar(FiltroMovimentacao filtro);
}


