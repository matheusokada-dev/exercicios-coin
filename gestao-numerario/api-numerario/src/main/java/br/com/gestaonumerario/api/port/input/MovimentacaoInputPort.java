package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.model.FiltroMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.Movimentacao;
import br.com.gestaonumerario.api.core.domain.model.Pagina;

public interface MovimentacaoInputPort {
    Pagina<Movimentacao> consultar(FiltroMovimentacao filtro);
}
