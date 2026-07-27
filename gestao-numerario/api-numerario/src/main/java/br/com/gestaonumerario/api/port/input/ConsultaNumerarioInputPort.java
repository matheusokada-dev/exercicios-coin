package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.*;
import java.util.List;

public interface ConsultaNumerarioInputPort {
    Pagina<OperacaoNumerario> consultarOperacoes(FiltroOperacaoNumerario filtro);
    List<UnidadeOperacional> consultarUnidades(TipoUnidadeOperacional tipo);
}
