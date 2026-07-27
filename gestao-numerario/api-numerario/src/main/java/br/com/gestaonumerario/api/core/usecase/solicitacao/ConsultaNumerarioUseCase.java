package br.com.gestaonumerario.api.core.usecase.solicitacao;

import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.*;
import br.com.gestaonumerario.api.port.input.ConsultaNumerarioInputPort;
import br.com.gestaonumerario.api.port.output.NumerarioOutputPort;
import java.util.List;

public class ConsultaNumerarioUseCase implements ConsultaNumerarioInputPort {
    private final NumerarioOutputPort port;

    public ConsultaNumerarioUseCase(NumerarioOutputPort port) {
        this.port=port;
    }

    @Override
    public Pagina<OperacaoNumerario> consultarOperacoes(FiltroOperacaoNumerario filtro) {
        return port.consultarOperacoes(filtro);
    }

    @Override
    public List<UnidadeOperacional> consultarUnidades(TipoUnidadeOperacional tipo) {
        return port.consultarUnidadesAtivas(tipo);
    }
}
