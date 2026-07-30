package br.com.gestaonumerario.api.port.output;

import br.com.gestaonumerario.api.core.domain.model.FiltroSolicitacao;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoAbastecimento;
import java.util.Optional;

public interface SolicitacaoAbastecimentoOutputPort {

    Optional<SolicitacaoAbastecimento> buscarPorId(Long id);

    Optional<SolicitacaoAbastecimento> buscarAprovadaPorAgenciaId(Long agenciaId);

    Pagina<SolicitacaoAbastecimento> buscar(FiltroSolicitacao filtro);

    boolean existeSolicitacaoAbertaParaAgencia(Long agenciaId);

    SolicitacaoAbastecimento salvar(SolicitacaoAbastecimento solicitacao);
}
