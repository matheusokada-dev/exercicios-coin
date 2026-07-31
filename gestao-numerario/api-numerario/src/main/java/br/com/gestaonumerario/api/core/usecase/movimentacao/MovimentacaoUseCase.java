package br.com.gestaonumerario.api.core.usecase.movimentacao;

import br.com.gestaonumerario.api.core.domain.model.FiltroMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.Movimentacao;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.port.input.MovimentacaoInputPort;
import br.com.gestaonumerario.api.port.output.MovimentacaoOutputPort;

public class MovimentacaoUseCase implements MovimentacaoInputPort {

    private final MovimentacaoOutputPort movimentacaoPort;

    public MovimentacaoUseCase(MovimentacaoOutputPort movimentacaoPort) {
        this.movimentacaoPort = movimentacaoPort;
    }

    @Override
    public Pagina<Movimentacao> consultar(FiltroMovimentacao filtro) {
        if (filtro == null) {
            throw new CampoObrigatorioException("filtro");
        }
        return movimentacaoPort.buscar(filtro);
    }
}
