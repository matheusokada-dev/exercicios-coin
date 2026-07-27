package br.com.gestaonumerario.api.adapter.output.persistence;

import br.com.gestaonumerario.api.adapter.output.mapper.*;
import br.com.gestaonumerario.api.adapter.output.repository.*;
import br.com.gestaonumerario.api.adapter.output.repository.entity.*;
import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.*;
import br.com.gestaonumerario.api.port.output.NumerarioOutputPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneOffset;
import java.util.*;

@Component @RequiredArgsConstructor
public class NumerarioPersistenceAdapter implements NumerarioOutputPort {
    private final SolicitacaoAbastecimentoJpaRepository solicitacaoRepository;
    private final OperacaoNumerarioJpaRepository operacaoRepository;
    private final HistoricoSolicitacaoNumerarioJpaRepository historicoRepository;
    private final UnidadeOperacionalJpaRepository unidadeRepository;
    private final MovimentacaoJpaRepository movimentacaoRepository;
    private final AgenciaJpaRepository agenciaRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final ComandoIdempotenteJpaRepository comandoRepository;
    private final SolicitacaoNumerarioPersistenceMapper solicitacaoMapper;
    private final OperacaoNumerarioPersistenceMapper operacaoMapper;
    private final UnidadeOperacionalPersistenceMapper unidadeMapper;
    private final UsuarioPersistenceMapper usuarioMapper;
    private final ObjectMapper objectMapper;

    @Override @Transactional(readOnly=true)
    public Pagina<SolicitacaoNumerario> consultarSolicitacoes(FiltroSolicitacaoNumerario f) {
        var page=solicitacaoRepository.buscarComFiltros(f.agenciaId(),f.tipo(),f.status(),f.origemId(),f.destinoId(),
                inicio(f.dataInicio()),fim(f.dataFim()),PageRequest.of(f.pagina(),f.tamanho(),
                        Sort.by(Sort.Direction.DESC,"dataCriacao")));
        return new Pagina<>(page.map(solicitacaoMapper::toDomain).getContent(),page.getNumber(),
                page.getSize(),page.getTotalElements(),page.getTotalPages());
    }

    @Override @Transactional(readOnly=true)
    public Optional<DetalheSolicitacaoNumerario> buscarDetalhe(Long id) {
        return solicitacaoRepository.findById(id).map(entity -> {
            var solicitacao=solicitacaoMapper.toDomain(entity);
            var operacao=operacaoRepository.findBySolicitacao_Id(id).map(operacaoMapper::toDomain).orElse(null);
            var historico=historicoRepository.findBySolicitacao_IdOrderByDataEventoAscIdAsc(id).stream()
                    .map(h -> new HistoricoSolicitacaoNumerario(h.getId(),id,
                            h.getOperacao()==null?null:h.getOperacao().getId(),h.getEvento(),
                            h.getStatusAnterior(),h.getStatusNovo(),h.getUsuario().getId(),
                            h.getDataEvento(),h.getJustificativa(),lerJson(h.getDadosComplementares())))
                    .toList();
            return new DetalheSolicitacaoNumerario(solicitacao,operacao,historico);
        });
    }

    @Override @Transactional(readOnly=true)
    public Pagina<OperacaoNumerario> consultarOperacoes(FiltroOperacaoNumerario f) {
        var page=operacaoRepository.buscar(f.status(),f.origemId(),f.destinoId(),
                inicio(f.dataInicio()),fim(f.dataFim()),PageRequest.of(f.pagina(),f.tamanho(),
                        Sort.by(Sort.Direction.DESC,"dataProgramacao")));
        return new Pagina<>(page.map(operacaoMapper::toDomain).getContent(),page.getNumber(),
                page.getSize(),page.getTotalElements(),page.getTotalPages());
    }

    @Override @Transactional(readOnly=true)
    public List<UnidadeOperacional> consultarUnidadesAtivas(TipoUnidadeOperacional tipo) {
        var entidades=tipo==null?unidadeRepository.findByAtivoTrueOrderByNomeAsc()
                :unidadeRepository.findByTipoAndAtivoTrueOrderByNomeAsc(tipo);
        return entidades.stream().map(unidadeMapper::toDomain).toList();
    }

    @Override @Transactional(readOnly=true)
    public Optional<UnidadeOperacional> buscarUnidade(Long id) {
        return unidadeRepository.findById(id).map(unidadeMapper::toDomain);
    }

    @Override
    public Optional<UnidadeOperacional> buscarUnidadeParaAtualizacao(Long id) {
        return unidadeRepository.buscarParaAtualizacao(id).map(unidadeMapper::toDomain);
    }

    @Override @Transactional(readOnly=true)
    public Optional<UnidadeOperacional> buscarUnidadeDaAgencia(Long agenciaId) {
        return unidadeRepository.findByAgencia_Id(agenciaId).map(unidadeMapper::toDomain);
    }

    @Override @Transactional(readOnly=true)
    public boolean existeSolicitacaoAberta(Long agenciaId) {
        return solicitacaoRepository.existsByAgencia_IdAndStatusIn(agenciaId,
                EnumSet.of(br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario.PENDENTE,
                        br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario.APROVADA,
                        br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario.EM_EXECUCAO,
                        br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario.COM_DIVERGENCIA));
    }

    @Override @Transactional(readOnly=true)
    public boolean existeIdempotencyKey(String key) {
        return key!=null && (comandoRepository.existsByIdempotencyKey(key)
                || operacaoRepository.existsByIdempotencyKey(key)
                || movimentacaoRepository.existsByIdempotencyKey(key));
    }

    @Override @Transactional(readOnly=true)
    public boolean existeComandoDoTipo(String tipoComando) {
        return comandoRepository.existsByTipoComando(tipoComando);
    }

    @Override @Transactional
    public void registrarIdempotencia(String key,String tipo,Long operacaoId,
                                      Long usuarioId,java.time.Instant data) {
        try {
            var operacao=operacaoId==null?null:operacaoRepository.getReferenceById(operacaoId);
            comandoRepository.saveAndFlush(new ComandoIdempotenteEntity(key,tipo,operacao,
                    usuarioRepository.getReferenceById(usuarioId),data));
        } catch(org.springframework.dao.DataIntegrityViolationException e) {
            throw new br.com.gestaonumerario.api.core.exception.IdempotencyKeyDuplicadaException();
        }
    }

    @Override @Transactional
    public SolicitacaoNumerario salvarSolicitacao(SolicitacaoNumerario s) {
        var salvo=persistirSolicitacao(s);
        salvarEventos(s.getEventosNovos(),salvo,null);
        return solicitacaoMapper.toDomain(salvo);
    }

    private SolicitacaoAbastecimentoEntity persistirSolicitacao(SolicitacaoNumerario s) {
        AgenciaEntity agencia = agenciaRepository.getReferenceById(s.getAgenciaReferenciaId());
        UnidadeOperacionalEntity origem = referenciaUnidade(s.getOrigem());
        UnidadeOperacionalEntity destino = referenciaUnidade(s.getDestino());
        UsuarioEntity solicitante = usuarioRepository.getReferenceById(s.getSolicitante().getId());
        UsuarioEntity aprovador = referenciaUsuario(s.getAprovador());
        UsuarioEntity canceladoPor = referenciaUsuario(s.getCanceladoPor());
        var entity = new SolicitacaoAbastecimentoEntity(s.getId(),s.getTipoOperacao(),agencia,
                origem,destino,s.getValorSolicitado(),s.getMotivo(),s.getDataDesejada(),s.getStatus(),
                solicitante,aprovador,s.getJustificativaDecisao(),s.getDataCriacao(),s.getDataDecisao(),
                s.getDataConclusao(),canceladoPor,s.getJustificativaCancelamento(),
                s.getDataCancelamento(),s.getVersao());
        var salvo=solicitacaoRepository.saveAndFlush(entity);
        return salvo;
    }

    @Override @Transactional
    public OperacaoNumerario salvarOperacao(OperacaoNumerario o) {
        var atual=solicitacaoRepository.findById(o.getSolicitacao().getId()).orElseThrow();
        var solicitacao=atual.getStatus()==o.getSolicitacao().getStatus()
                ? atual:persistirSolicitacao(o.getSolicitacao());
        var salvo=operacaoRepository.saveAndFlush(entidadeOperacao(o,solicitacao));
        salvarEventos(o.getSolicitacao().getEventosNovos(),solicitacao,salvo);
        return operacaoMapper.toDomain(salvo);
    }

    private OperacaoNumerarioEntity entidadeOperacao(OperacaoNumerario o,
            SolicitacaoAbastecimentoEntity solicitacao) {
        return new OperacaoNumerarioEntity(o.getId(),solicitacao,
                referenciaUnidade(o.getOrigem()),referenciaUnidade(o.getDestino()),o.getStatus(),
                o.getValorProgramado(),o.getValorExpedido(),o.getValorRecebido(),o.getValorDivergencia(),
                referenciaUsuario(o.getProgramadoPor()),referenciaUsuario(o.getExpedidoPor()),
                referenciaUsuario(o.getRecebidoPor()),referenciaUsuario(o.getConciliadoPor()),
                o.getDataProgramacao(),o.getDataExpedicao(),o.getDataRecebimento(),o.getDataConciliacao(),
                o.getJustificativaDivergencia(),o.getDescricaoOcorrencia(),o.getIdempotencyKey(),o.getVersao());
    }

    @Override @Transactional
    public OperacaoNumerario salvarProgramacao(OperacaoNumerario o) {
        return salvarOperacao(o);
    }

    @Override @Transactional
    public OperacaoNumerario salvarOperacaoFinanceira(OperacaoNumerario o,
            br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao tipo,
            Long usuarioId,String key,String descricao,java.time.Instant data) {
        boolean entrada=tipo==br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao.ENTRADA_DE_TRANSITO;
        UnidadeOperacional unidade=entrada?o.getDestino():o.getOrigem();
        java.math.BigDecimal valor=entrada?o.getValorRecebido():o.getValorExpedido();
        java.math.BigDecimal posterior=unidade.getSaldoAtual();
        java.math.BigDecimal anterior=entrada?posterior.subtract(valor):posterior.add(valor);
        var unidadeSalva=unidadeRepository.saveAndFlush(unidadeMapper.toEntity(unidade));
        OperacaoNumerario operacaoSalva=salvarOperacao(o);
        var operacaoEntity=operacaoRepository.getReferenceById(operacaoSalva.getId());
        try {
            movimentacaoRepository.saveAndFlush(new MovimentacaoEntity(unidadeSalva,
                    solicitacaoRepository.getReferenceById(o.getSolicitacao().getId()),operacaoEntity,
                    tipo,entrada,valor,anterior,posterior,descricao,data,
                    usuarioRepository.getReferenceById(usuarioId),key));
        } catch(org.springframework.dao.DataIntegrityViolationException e) {
            throw new br.com.gestaonumerario.api.core.exception.IdempotencyKeyDuplicadaException();
        }
        return operacaoSalva;
    }

    @Override @Transactional
    public UnidadeOperacional salvarUnidade(UnidadeOperacional unidade) {
        return unidadeMapper.toDomain(unidadeRepository.saveAndFlush(unidadeMapper.toEntity(unidade)));
    }

    @Override @Transactional
    public UnidadeOperacional salvarAjusteFinanceiro(UnidadeOperacional unidade,Long operacaoId,
            br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao tipo,boolean entrada,
            java.math.BigDecimal valor,Long usuarioId,String key,String descricao,
            java.time.Instant data) {
        java.math.BigDecimal posterior=unidade.getSaldoAtual();
        java.math.BigDecimal anterior=entrada?posterior.subtract(valor):posterior.add(valor);
        var unidadeSalva=unidadeRepository.saveAndFlush(unidadeMapper.toEntity(unidade));
        var operacao=operacaoId==null?null:operacaoRepository.getReferenceById(operacaoId);
        var solicitacao=operacao==null?null:operacao.getSolicitacao();
        try {
            movimentacaoRepository.saveAndFlush(new MovimentacaoEntity(unidadeSalva,solicitacao,
                    operacao,tipo,entrada,valor,anterior,posterior,descricao,data,
                    usuarioRepository.getReferenceById(usuarioId),key));
        } catch(org.springframework.dao.DataIntegrityViolationException e) {
            throw new br.com.gestaonumerario.api.core.exception.IdempotencyKeyDuplicadaException();
        }
        return unidadeMapper.toDomain(unidadeSalva);
    }

    private void salvarEventos(List<HistoricoSolicitacaoNumerario> eventos,
                               SolicitacaoAbastecimentoEntity solicitacao,
                               OperacaoNumerarioEntity operacaoPadrao) {
        for(var e:eventos) {
            var operacao=e.operacaoId()==null?operacaoPadrao:operacaoRepository.getReferenceById(e.operacaoId());
            historicoRepository.save(new HistoricoSolicitacaoNumerarioEntity(solicitacao,operacao,
                    e.evento(),e.statusAnterior(),e.statusNovo(),
                    usuarioRepository.getReferenceById(e.usuarioId()),e.dataEvento(),
                    e.justificativa(),escreverJson(e.dadosComplementares())));
        }
    }

    private UnidadeOperacionalEntity referenciaUnidade(UnidadeOperacional u) {
        return u==null?null:unidadeRepository.getReferenceById(u.getId());
    }
    private UsuarioEntity referenciaUsuario(Usuario u) {
        return u==null?null:usuarioRepository.getReferenceById(u.getId());
    }
    private String escreverJson(Map<String,Object> dados) {
        try { return objectMapper.writeValueAsString(dados); }
        catch(Exception e) { throw new IllegalStateException("Falha ao serializar histórico.",e); }
    }

    private static java.time.Instant inicio(java.time.LocalDate data) {
        return data==null?null:data.atStartOfDay(ZoneOffset.UTC).toInstant();
    }
    private static java.time.Instant fim(java.time.LocalDate data) {
        return data==null?null:data.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
    private Map<String,Object> lerJson(String json) {
        if(json==null||json.isBlank()) return Map.of();
        try { return objectMapper.readValue(json,new TypeReference<>(){}); }
        catch(Exception e) { return Map.of("conteudoOriginal",json); }
    }
}
