package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.model.*;
import br.com.gestaonumerario.api.core.domain.model.command.*;

public interface SolicitacaoNumerarioInputPort {
    SolicitacaoNumerario criar(CriarSolicitacaoNumerarioCommand command);
    SolicitacaoNumerario aprovar(DecidirSolicitacaoNumerarioCommand command);
    SolicitacaoNumerario rejeitar(DecidirSolicitacaoNumerarioCommand command);
    SolicitacaoNumerario cancelar(DecidirSolicitacaoNumerarioCommand command);
    Pagina<SolicitacaoNumerario> consultar(FiltroSolicitacaoNumerario filtro);
    DetalheSolicitacaoNumerario detalhar(Long id);
}
