package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.model.DetalheSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.FiltroSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.command.CriarSolicitacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.DecidirSolicitacaoNumerarioCommand;

public interface SolicitacaoNumerarioInputPort {
    SolicitacaoNumerario criar(CriarSolicitacaoNumerarioCommand command);

    SolicitacaoNumerario aprovar(DecidirSolicitacaoNumerarioCommand command);

    SolicitacaoNumerario rejeitar(DecidirSolicitacaoNumerarioCommand command);

    SolicitacaoNumerario cancelar(DecidirSolicitacaoNumerarioCommand command);

    Pagina<SolicitacaoNumerario> consultar(FiltroSolicitacaoNumerario filtro);

    DetalheSolicitacaoNumerario detalhar(Long id);
}
