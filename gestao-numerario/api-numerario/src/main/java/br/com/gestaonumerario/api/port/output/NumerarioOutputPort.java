package br.com.gestaonumerario.api.port.output;

import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.*;
import java.util.List;
import java.util.Optional;

public interface NumerarioOutputPort {
    Pagina<SolicitacaoNumerario> consultarSolicitacoes(FiltroSolicitacaoNumerario filtro);
    Optional<DetalheSolicitacaoNumerario> buscarDetalhe(Long solicitacaoId);
    Pagina<OperacaoNumerario> consultarOperacoes(FiltroOperacaoNumerario filtro);
    List<UnidadeOperacional> consultarUnidadesAtivas(TipoUnidadeOperacional tipo);
    Optional<UnidadeOperacional> buscarUnidade(Long id);
    Optional<UnidadeOperacional> buscarUnidadeParaAtualizacao(Long id);
    Optional<UnidadeOperacional> buscarUnidadeDaAgencia(Long agenciaId);
    boolean existeSolicitacaoAberta(Long agenciaId);
    boolean existeIdempotencyKey(String key);
    boolean existeComandoDoTipo(String tipoComando);
    void registrarIdempotencia(String key, String tipoComando, Long operacaoId,
                               Long usuarioId, java.time.Instant data);
    SolicitacaoNumerario salvarSolicitacao(SolicitacaoNumerario solicitacao);
    OperacaoNumerario salvarOperacao(OperacaoNumerario operacao);
    OperacaoNumerario salvarProgramacao(OperacaoNumerario operacao);
    OperacaoNumerario salvarOperacaoFinanceira(OperacaoNumerario operacao,
            br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao tipo,
            Long usuarioId, String idempotencyKey, String descricao,
            java.time.Instant data);
    UnidadeOperacional salvarUnidade(UnidadeOperacional unidade);
    UnidadeOperacional salvarAjusteFinanceiro(UnidadeOperacional unidade,
            Long operacaoId, br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao tipo,
            boolean entrada, java.math.BigDecimal valor, Long usuarioId,
            String idempotencyKey, String descricao, java.time.Instant data);
}
