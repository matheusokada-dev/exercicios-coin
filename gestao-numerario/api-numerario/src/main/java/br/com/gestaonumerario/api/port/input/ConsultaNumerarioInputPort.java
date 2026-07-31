package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.FiltroOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.OperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.UnidadeOperacional;
import java.util.List;

public interface ConsultaNumerarioInputPort {
    Pagina<OperacaoNumerario> consultarOperacoes(FiltroOperacaoNumerario filtro);

    List<UnidadeOperacional> consultarUnidades(TipoUnidadeOperacional tipo);
}
