package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.model.FiltroSolicitacao;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoAbastecimento;
import br.com.gestaonumerario.api.core.domain.model.command.AprovarSolicitacaoCommand;
import br.com.gestaonumerario.api.core.domain.model.command.AtenderSolicitacaoCommand;
import br.com.gestaonumerario.api.core.domain.model.command.RejeitarSolicitacaoCommand;
import br.com.gestaonumerario.api.core.domain.model.command.SolicitarAbastecimentoCommand;

public interface SolicitacaoInputPort {
    SolicitacaoAbastecimento solicitar(SolicitarAbastecimentoCommand command);

    SolicitacaoAbastecimento aprovar(AprovarSolicitacaoCommand command);

    SolicitacaoAbastecimento rejeitar(RejeitarSolicitacaoCommand command);

    SolicitacaoAbastecimento atender(AtenderSolicitacaoCommand command);

    Pagina<SolicitacaoAbastecimento> consultar(FiltroSolicitacao filtro);
}
