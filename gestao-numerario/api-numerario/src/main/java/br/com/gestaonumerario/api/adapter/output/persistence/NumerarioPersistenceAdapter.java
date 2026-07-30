package br.com.gestaonumerario.api.adapter.output.persistence;

import br.com.gestaonumerario.api.adapter.output.mapper.OperacaoNumerarioPersistenceMapper;
import br.com.gestaonumerario.api.adapter.output.mapper.SolicitacaoNumerarioPersistenceMapper;
import br.com.gestaonumerario.api.adapter.output.mapper.UnidadeOperacionalVirtualMapper;
import br.com.gestaonumerario.api.adapter.output.mapper.UsuarioPersistenceMapper;
import br.com.gestaonumerario.api.adapter.output.repository.AgenciaJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.ComandoIdempotenteJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.HistoricoSolicitacaoNumerarioJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.MovimentacaoJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.OperacaoNumerarioJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.SolicitacaoAbastecimentoJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.UsuarioJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.ComandoIdempotenteEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.HistoricoSolicitacaoNumerarioEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.MovimentacaoEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.OperacaoNumerarioEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.SolicitacaoAbastecimentoEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.UsuarioEntity;
import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.DetalheSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.FiltroOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.FiltroSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.HistoricoSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.OperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.UnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.port.output.NumerarioOutputPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NumerarioPersistenceAdapter implements NumerarioOutputPort {
    private final SolicitacaoAbastecimentoJpaRepository solicitacaoRepository;
    private final OperacaoNumerarioJpaRepository operacaoRepository;
    private final HistoricoSolicitacaoNumerarioJpaRepository historicoRepository;
    private final MovimentacaoJpaRepository movimentacaoRepository;
    private final AgenciaJpaRepository agenciaRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final ComandoIdempotenteJpaRepository comandoRepository;
    private final SolicitacaoNumerarioPersistenceMapper solicitacaoMapper;
    private final OperacaoNumerarioPersistenceMapper operacaoMapper;
    private final UnidadeOperacionalVirtualMapper unidadeMapper;
    private final UsuarioPersistenceMapper usuarioMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Pagina<SolicitacaoNumerario> consultarSolicitacoes(FiltroSolicitacaoNumerario f) {
        var page = solicitacaoRepository.buscarComFiltros(
                f.agenciaId(),
                f.tipo(),
                f.status(),
                f.origemId(),
                f.destinoId(),
                inicio(f.dataInicio()),
                fim(f.dataFim()),
                PageRequest.of(
                        f.pagina(),
                        f.tamanho(),
                        Sort.by(
                                Sort.Direction.DESC,
                                "dataCriacao"
                        )
                )
        );
        return new Pagina<>(
                page.map(solicitacaoMapper::toDomain)
                        .getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DetalheSolicitacaoNumerario> buscarDetalhe(Long id) {
        return solicitacaoRepository.findById(id)
                .map(entity -> {
                    var solicitacao = solicitacaoMapper.toDomain(entity);
                    var operacao = operacaoRepository.findBySolicitacao_Id(id)
                            .map(operacaoMapper::toDomain)
                            .orElse(null);
                    var historico = historicoRepository.findBySolicitacao_IdOrderByDataEventoAscIdAsc(id)
                            .stream()
                            .map(
                                    h -> new HistoricoSolicitacaoNumerario(
                                            h.getId(),
                                            id,
                                            h.getOperacao() == null
                                                    ? null
                                                    : h.getOperacao()
                                                            .getId(),
                                            h.getEvento(),
                                            h.getStatusAnterior(),
                                            h.getStatusNovo(),
                                            h.getUsuario()
                                                    .getId(),
                                            h.getDataEvento(),
                                            h.getJustificativa(),
                                            lerJson(h.getDadosComplementares())
                                    )
                            )
                            .toList();
                    return new DetalheSolicitacaoNumerario(
                            solicitacao,
                            operacao,
                            historico
                    );
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<OperacaoNumerario> consultarOperacoes(FiltroOperacaoNumerario f) {
        var page = operacaoRepository.buscar(
                f.status(),
                f.origemId(),
                f.destinoId(),
                inicio(f.dataInicio()),
                fim(f.dataFim()),
                PageRequest.of(
                        f.pagina(),
                        f.tamanho(),
                        Sort.by(
                                Sort.Direction.DESC,
                                "dataProgramacao"
                        )
                )
        );
        return new Pagina<>(
                page.map(operacaoMapper::toDomain)
                        .getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadeOperacional> consultarUnidadesAtivas(TipoUnidadeOperacional tipo) {
        var agencias = agenciaRepository.findAll()
                .stream()
                .filter(AgenciaEntity::isAtivo)
                .map(unidadeMapper::agencia)
                .toList();
        if (tipo == TipoUnidadeOperacional.TESOURARIA) {
            return List.of(unidadeMapper.tesouraria());
        }
        if (tipo == TipoUnidadeOperacional.AGENCIA) {
            return agencias;
        }
        if (tipo == null) {
            return agencias;
        }
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UnidadeOperacional> buscarUnidade(Long id) {
        if (id != null && id == UnidadeOperacionalVirtualMapper.TESOURARIA_ID) {
            return Optional.of(unidadeMapper.tesouraria());
        }
        return agenciaRepository.findById(id).map(unidadeMapper::agencia);
    }

    @Override
    public Optional<UnidadeOperacional> buscarUnidadeParaAtualizacao(Long id) {
        if (id != null && id == UnidadeOperacionalVirtualMapper.TESOURARIA_ID) {
            return Optional.of(unidadeMapper.tesouraria());
        }
        return agenciaRepository.buscarPorIdParaAtualizacao(id).map(unidadeMapper::agencia);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UnidadeOperacional> buscarUnidadeDaAgencia(Long agenciaId) {
        return agenciaRepository.findById(agenciaId).map(unidadeMapper::agencia);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeSolicitacaoAberta(Long agenciaId) {
        return solicitacaoRepository.existsByAgencia_IdAndStatusIn(
                agenciaId,
                EnumSet.of(
                        br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario.PENDENTE,
                        br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario.APROVADA,
                        br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario.EM_EXECUCAO,
                        br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario.COM_DIVERGENCIA
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeIdempotencyKey(String key) {
        return key != null
                && (comandoRepository.existsByIdempotencyKey(key) || operacaoRepository.existsByIdempotencyKey(key)
                        || movimentacaoRepository.existsByIdempotencyKey(key));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeComandoDoTipo(String tipoComando) {
        return comandoRepository.existsByTipoComando(tipoComando);
    }

    @Override
    @Transactional
    public void registrarIdempotencia(
            String key,
            String tipo,
            Long operacaoId,
            Long usuarioId,
            java.time.Instant data) {
        try {
            var operacao = operacaoId == null ? null : operacaoRepository.getReferenceById(operacaoId);
            comandoRepository.saveAndFlush(
                    new ComandoIdempotenteEntity(
                            key,
                            tipo,
                            operacao,
                            usuarioRepository.getReferenceById(usuarioId),
                            data
                    )
            );
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new br.com.gestaonumerario.api.core.exception.IdempotencyKeyDuplicadaException();
        }
    }

    @Override
    @Transactional
    public SolicitacaoNumerario salvarSolicitacao(SolicitacaoNumerario s) {
        var salvo = persistirSolicitacao(s);
        salvarEventos(
                s.getEventosNovos(),
                salvo,
                null
        );
        return solicitacaoMapper.toDomain(salvo);
    }

    private SolicitacaoAbastecimentoEntity persistirSolicitacao(SolicitacaoNumerario s) {
        AgenciaEntity agencia = agenciaRepository.getReferenceById(s.getAgenciaReferenciaId());
        UsuarioEntity solicitante = usuarioRepository.getReferenceById(
                s.getSolicitante()
                        .getId()
        );
        UsuarioEntity aprovador = referenciaUsuario(s.getAprovador());
        UsuarioEntity canceladoPor = referenciaUsuario(s.getCanceladoPor());
        var entity = new SolicitacaoAbastecimentoEntity(
                s.getId(),
                s.getTipoOperacao(),
                agencia,
                referenciaAgencia(s.getOrigem()),
                referenciaAgencia(s.getDestino()),
                s.getValorSolicitado(),
                s.getMotivo(),
                s.getDataDesejada(),
                s.getStatus(),
                solicitante,
                aprovador,
                s.getJustificativaDecisao(),
                s.getDataCriacao(),
                s.getDataDecisao(),
                s.getDataConclusao(),
                canceladoPor,
                s.getJustificativaCancelamento(),
                s.getDataCancelamento(),
                s.getVersao()
        );
        var salvo = solicitacaoRepository.saveAndFlush(entity);
        return salvo;
    }

    @Override
    @Transactional
    public OperacaoNumerario salvarOperacao(OperacaoNumerario o) {
        var atual = solicitacaoRepository.findById(
                o.getSolicitacao()
                        .getId()
        )
                .orElseThrow();
        var solicitacao = atual.getStatus() == o.getSolicitacao()
                .getStatus() ? atual : persistirSolicitacao(o.getSolicitacao());
        var salvo = operacaoRepository.saveAndFlush(
                entidadeOperacao(
                        o,
                        solicitacao
                )
        );
        salvarEventos(
                o.getSolicitacao()
                        .getEventosNovos(),
                solicitacao,
                salvo
        );
        return operacaoMapper.toDomain(salvo);
    }

    private OperacaoNumerarioEntity entidadeOperacao(OperacaoNumerario o, SolicitacaoAbastecimentoEntity solicitacao) {
        return new OperacaoNumerarioEntity(
                o.getId(),
                solicitacao,
                referenciaAgencia(o.getOrigem()),
                referenciaAgencia(o.getDestino()),
                o.getStatus(),
                o.getValorProgramado(),
                o.getValorExpedido(),
                o.getValorRecebido(),
                o.getValorDivergencia(),
                referenciaUsuario(o.getProgramadoPor()),
                referenciaUsuario(o.getExpedidoPor()),
                referenciaUsuario(o.getRecebidoPor()),
                referenciaUsuario(o.getConciliadoPor()),
                o.getDataProgramacao(),
                o.getDataExpedicao(),
                o.getDataRecebimento(),
                o.getDataConciliacao(),
                o.getJustificativaDivergencia(),
                o.getDescricaoOcorrencia(),
                o.getIdempotencyKey(),
                o.getVersao()
        );
    }

    @Override
    @Transactional
    public OperacaoNumerario salvarProgramacao(OperacaoNumerario o) {
        return salvarOperacao(o);
    }

    @Override
    @Transactional
    public OperacaoNumerario salvarOperacaoFinanceira(
            OperacaoNumerario o,
            br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao tipo,
            Long usuarioId,
            String key,
            String descricao,
            java.time.Instant data) {
        boolean entrada = tipo == br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao.ENTRADA_DE_TRANSITO;
        UnidadeOperacional unidade = entrada ? o.getDestino() : o.getOrigem();
        java.math.BigDecimal valor = entrada ? o.getValorRecebido() : o.getValorExpedido();
        java.math.BigDecimal posterior = unidade.getSaldoAtual();
        java.math.BigDecimal anterior = entrada ? posterior.subtract(valor) : posterior.add(valor);
        OperacaoNumerario operacaoSalva = salvarOperacao(o);
        if (unidade.getId() == UnidadeOperacionalVirtualMapper.TESOURARIA_ID) {
            return operacaoSalva;
        }
        var agenciaSalva = agenciaRepository.buscarPorIdParaAtualizacao(unidade.getId())
                .orElseThrow();
        agenciaSalva.atualizarSaldo(posterior);
        agenciaRepository.saveAndFlush(agenciaSalva);
        var operacaoEntity = operacaoRepository.getReferenceById(operacaoSalva.getId());
        try {
            movimentacaoRepository.saveAndFlush(
                    new MovimentacaoEntity(
                            agenciaSalva,
                            solicitacaoRepository.getReferenceById(
                                    o.getSolicitacao()
                                            .getId()
                            ),
                            operacaoEntity,
                            tipo,
                            entrada,
                            valor,
                            anterior,
                            posterior,
                            descricao,
                            data,
                            usuarioRepository.getReferenceById(usuarioId),
                            key
                    )
            );
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new br.com.gestaonumerario.api.core.exception.IdempotencyKeyDuplicadaException();
        }
        return operacaoSalva;
    }

    @Override
    @Transactional
    public UnidadeOperacional salvarUnidade(UnidadeOperacional unidade) {
        if (unidade.getId() == UnidadeOperacionalVirtualMapper.TESOURARIA_ID) {
            return unidade;
        }
        var agencia = agenciaRepository.buscarPorIdParaAtualizacao(unidade.getId())
                .orElseThrow();
        agencia.atualizarSaldo(unidade.getSaldoAtual());
        return unidadeMapper.agencia(agenciaRepository.saveAndFlush(agencia));
    }

    @Override
    @Transactional
    public UnidadeOperacional salvarAjusteFinanceiro(
            UnidadeOperacional unidade,
            Long operacaoId,
            br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao tipo,
            boolean entrada,
            java.math.BigDecimal valor,
            Long usuarioId,
            String key,
            String descricao,
            java.time.Instant data) {
        java.math.BigDecimal posterior = unidade.getSaldoAtual();
        java.math.BigDecimal anterior = entrada ? posterior.subtract(valor) : posterior.add(valor);
        if (unidade.getId() == UnidadeOperacionalVirtualMapper.TESOURARIA_ID) {
            return unidade;
        }
        var agenciaSalva = agenciaRepository.buscarPorIdParaAtualizacao(unidade.getId())
                .orElseThrow();
        agenciaSalva.atualizarSaldo(posterior);
        agenciaRepository.saveAndFlush(agenciaSalva);
        var operacao = operacaoId == null ? null : operacaoRepository.getReferenceById(operacaoId);
        var solicitacao = operacao == null ? null : operacao.getSolicitacao();
        try {
            movimentacaoRepository.saveAndFlush(
                    new MovimentacaoEntity(
                            agenciaSalva,
                            solicitacao,
                            operacao,
                            tipo,
                            entrada,
                            valor,
                            anterior,
                            posterior,
                            descricao,
                            data,
                            usuarioRepository.getReferenceById(usuarioId),
                            key
                    )
            );
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new br.com.gestaonumerario.api.core.exception.IdempotencyKeyDuplicadaException();
        }
        return unidadeMapper.agencia(agenciaSalva);
    }

    private void salvarEventos(
            List<HistoricoSolicitacaoNumerario> eventos,
            SolicitacaoAbastecimentoEntity solicitacao,
            OperacaoNumerarioEntity operacaoPadrao) {
        for (var e : eventos) {
            var operacao =
                    e.operacaoId() == null ? operacaoPadrao : operacaoRepository.getReferenceById(e.operacaoId());
            historicoRepository.save(
                    new HistoricoSolicitacaoNumerarioEntity(
                            solicitacao,
                            operacao,
                            e.evento(),
                            e.statusAnterior(),
                            e.statusNovo(),
                            usuarioRepository.getReferenceById(e.usuarioId()),
                            e.dataEvento(),
                            e.justificativa(),
                            escreverJson(e.dadosComplementares())
                    )
            );
        }
    }

    private UsuarioEntity referenciaUsuario(Usuario u) {
        return u == null ? null : usuarioRepository.getReferenceById(u.getId());
    }

    private AgenciaEntity referenciaAgencia(UnidadeOperacional unidade) {
        if (unidade == null || unidade.getId() == UnidadeOperacionalVirtualMapper.TESOURARIA_ID) {
            return null;
        }
        return agenciaRepository.getReferenceById(unidade.getId());
    }

    private String escreverJson(Map<String, Object> dados) {
        try {
            return objectMapper.writeValueAsString(dados);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Falha ao serializar histórico.",
                    e
            );
        }
    }

    private static java.time.Instant inicio(java.time.LocalDate data) {
        return data == null
                ? null
                : data.atStartOfDay(ZoneOffset.UTC)
                        .toInstant();
    }

    private static java.time.Instant fim(java.time.LocalDate data) {
        return data == null
                ? null
                : data.plusDays(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant();
    }

    private Map<String, Object> lerJson(String json) {
        if (json == null || json.isBlank())
            return Map.of();
        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            return Map.of(
                    "conteudoOriginal",
                    json
            );
        }
    }
}
