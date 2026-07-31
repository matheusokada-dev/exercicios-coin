package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.EventoHistoricoSolicitacao;
import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.exception.ApenasGestorPodeDecidirException;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.ConflitoVersaoException;
import br.com.gestaonumerario.api.core.exception.DataDesejadaNoPassadoException;
import br.com.gestaonumerario.api.core.exception.JustificativaObrigatoriaException;
import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import br.com.gestaonumerario.api.core.exception.TransicaoStatusInvalidaException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SolicitacaoNumerario {

    private final Long id;
    private final TipoOperacaoNumerario tipoOperacao;
    private final Long agenciaReferenciaId;
    private final BigDecimal valorSolicitado;
    private final String motivo;
    private final LocalDate dataDesejada;
    private final Usuario solicitante;
    private final Instant dataCriacao;
    private final List<HistoricoSolicitacaoNumerario> eventosNovos = new ArrayList<>();
    private UnidadeOperacional origem;
    private UnidadeOperacional destino;
    private StatusSolicitacaoNumerario status;
    private Usuario aprovador;
    private String justificativaDecisao;
    private Instant dataDecisao;
    private Usuario canceladoPor;
    private String justificativaCancelamento;
    private Instant dataCancelamento;
    private Instant dataConclusao;
    private long versao;

    private SolicitacaoNumerario(
            Long id,
            TipoOperacaoNumerario tipoOperacao,
            Long agenciaReferenciaId,
            UnidadeOperacional origem,
            UnidadeOperacional destino,
            BigDecimal valorSolicitado,
            String motivo,
            LocalDate dataDesejada,
            StatusSolicitacaoNumerario status,
            Usuario solicitante,
            Instant dataCriacao,
            long versao) {
        this.id = id;
        this.tipoOperacao = obrigatorio(tipoOperacao);
        this.agenciaReferenciaId = obrigatorio(agenciaReferenciaId);
        this.origem = origem;
        this.destino = destino;
        this.valorSolicitado = ValorMonetario.exigirPositivo(valorSolicitado);
        this.motivo = textoObrigatorio(motivo);
        this.dataDesejada = obrigatorio(dataDesejada);
        this.status = obrigatorio(status);
        this.solicitante = obrigatorio(solicitante);
        this.dataCriacao = obrigatorio(dataCriacao);
        this.versao = versao;
    }

    public static SolicitacaoNumerario criar(
            TipoOperacaoNumerario tipo,
            Long agenciaReferenciaId,
            UnidadeOperacional unidadeAgencia,
            BigDecimal valor,
            String motivo,
            LocalDate dataDesejada,
            Usuario solicitante,
            LocalDate hoje,
            Instant agora) {
        validarGestor(solicitante);
        if (hoje == null || dataDesejada == null || dataDesejada.isBefore(hoje)) {
            throw new DataDesejadaNoPassadoException();
        }
        if (unidadeAgencia == null || unidadeAgencia.getTipo() != TipoUnidadeOperacional.AGENCIA
                || !unidadeAgencia.isAtivo()) {
            throw new RegraOperacaoNumerarioException();
        }
        SolicitacaoNumerario solicitacao = new SolicitacaoNumerario(
                null,
                tipo,
                agenciaReferenciaId,
                tipo == TipoOperacaoNumerario.RECOLHIMENTO ? unidadeAgencia : null,
                tipo == TipoOperacaoNumerario.SUPRIMENTO ? unidadeAgencia : null,
                valor,
                motivo,
                dataDesejada,
                StatusSolicitacaoNumerario.PENDENTE,
                solicitante,
                agora,
                0
        );
        solicitacao.registrar(
                EventoHistoricoSolicitacao.SOLICITACAO_CRIADA,
                null,
                solicitacao.status.name(),
                solicitante,
                agora,
                motivo,
                Map.of(
                        "tipoOperacao",
                        tipo.name(),
                        "valorSolicitado",
                        valor
                )
        );
        return solicitacao;
    }

    public static SolicitacaoNumerario reconstituir(
            Long id,
            TipoOperacaoNumerario tipo,
            Long agenciaReferenciaId,
            UnidadeOperacional origem,
            UnidadeOperacional destino,
            BigDecimal valor,
            String motivo,
            LocalDate dataDesejada,
            StatusSolicitacaoNumerario status,
            Usuario solicitante,
            Usuario aprovador,
            String justificativaDecisao,
            Instant dataCriacao,
            Instant dataDecisao,
            Usuario canceladoPor,
            String justificativaCancelamento,
            Instant dataCancelamento,
            Instant dataConclusao,
            long versao) {
        SolicitacaoNumerario solicitacao = new SolicitacaoNumerario(
                id,
                tipo,
                agenciaReferenciaId,
                origem,
                destino,
                valor,
                motivo,
                dataDesejada,
                status,
                solicitante,
                dataCriacao,
                versao
        );
        solicitacao.aprovador = aprovador;
        solicitacao.justificativaDecisao = textoOpcional(justificativaDecisao);
        solicitacao.dataDecisao = dataDecisao;
        solicitacao.canceladoPor = canceladoPor;
        solicitacao.justificativaCancelamento = textoOpcional(justificativaCancelamento);
        solicitacao.dataCancelamento = dataCancelamento;
        solicitacao.dataConclusao = dataConclusao;
        return solicitacao;
    }

    public void aprovar(Usuario gestor, String justificativa, long versaoEsperada, Instant agora) {
        validarAlteracao(
                StatusSolicitacaoNumerario.PENDENTE,
                gestor,
                versaoEsperada
        );
        String texto = justificativaObrigatoria(justificativa);
        alterarStatus(StatusSolicitacaoNumerario.APROVADA);
        aprovador = gestor;
        justificativaDecisao = texto;
        dataDecisao = obrigatorio(agora);
        registrar(
                EventoHistoricoSolicitacao.SOLICITACAO_APROVADA,
                StatusSolicitacaoNumerario.PENDENTE.name(),
                status.name(),
                gestor,
                agora,
                texto,
                Map.of()
        );
    }

    public void rejeitar(Usuario gestor, String justificativa, long versaoEsperada, Instant agora) {
        validarAlteracao(
                StatusSolicitacaoNumerario.PENDENTE,
                gestor,
                versaoEsperada
        );
        String texto = justificativaObrigatoria(justificativa);
        alterarStatus(StatusSolicitacaoNumerario.REJEITADA);
        aprovador = gestor;
        justificativaDecisao = texto;
        dataDecisao = obrigatorio(agora);
        registrar(
                EventoHistoricoSolicitacao.SOLICITACAO_REJEITADA,
                StatusSolicitacaoNumerario.PENDENTE.name(),
                status.name(),
                gestor,
                agora,
                texto,
                Map.of()
        );
    }

    public void cancelar(Usuario usuario, String justificativa, long versaoEsperada, Instant agora) {
        validarVersaoEStatus(
                StatusSolicitacaoNumerario.PENDENTE,
                versaoEsperada
        );
        validarGestorOuSolicitante(usuario);
        String texto = justificativaObrigatoria(justificativa);
        alterarStatus(StatusSolicitacaoNumerario.CANCELADA);
        canceladoPor = usuario;
        justificativaCancelamento = texto;
        dataCancelamento = obrigatorio(agora);
        registrar(
                EventoHistoricoSolicitacao.SOLICITACAO_CANCELADA,
                StatusSolicitacaoNumerario.PENDENTE.name(),
                status.name(),
                usuario,
                agora,
                texto,
                Map.of()
        );
    }

    public OperacaoNumerario programar(
            UnidadeOperacional unidadeFaltante,
            Usuario gestor,
            String idempotencyKey,
            long versaoEsperada,
            Instant agora) {
        validarAlteracao(
                StatusSolicitacaoNumerario.APROVADA,
                gestor,
                versaoEsperada
        );
        validarUnidadeAtiva(unidadeFaltante);
        if (tipoOperacao == TipoOperacaoNumerario.SUPRIMENTO) {
            origem = unidadeFaltante;
        } else {
            destino = unidadeFaltante;
        }
        if (origem == null || destino == null || mesmaUnidade(
                origem,
                destino
        ) || !origem.isControlaSaldo() || !destino.isControlaSaldo()) {
            throw new RegraOperacaoNumerarioException();
        }
        alterarStatus(StatusSolicitacaoNumerario.EM_EXECUCAO);
        registrar(
                EventoHistoricoSolicitacao.ORIGEM_DESTINO_DEFINIDOS,
                StatusSolicitacaoNumerario.APROVADA.name(),
                status.name(),
                gestor,
                agora,
                null,
                Map.of(
                        "origemId",
                        origem.getId(),
                        "destinoId",
                        destino.getId()
                )
        );
        OperacaoNumerario operacao = OperacaoNumerario.programar(
                this,
                origem,
                destino,
                valorSolicitado,
                gestor,
                idempotencyKey,
                agora
        );
        registrar(
                EventoHistoricoSolicitacao.OPERACAO_PROGRAMADA,
                status.name(),
                status.name(),
                gestor,
                agora,
                null,
                Map.of(
                        "valorProgramado",
                        valorSolicitado
                )
        );
        return operacao;
    }

    void registrarExpedicao(Usuario gestor, Instant agora) {
        registrar(
                EventoHistoricoSolicitacao.NUMERARIO_EXPEDIDO,
                status.name(),
                status.name(),
                gestor,
                agora,
                null,
                Map.of()
        );
    }

    void registrarSeparacao(Usuario gestor, Instant agora) {
        registrar(
                EventoHistoricoSolicitacao.SEPARACAO_INICIADA,
                status.name(),
                status.name(),
                gestor,
                agora,
                null,
                Map.of()
        );
    }

    void registrarRecebimento(Usuario gestor, BigDecimal recebido, BigDecimal divergencia, Instant agora) {
        registrar(
                EventoHistoricoSolicitacao.NUMERARIO_RECEBIDO,
                status.name(),
                status.name(),
                gestor,
                agora,
                null,
                Map.of(
                        "valorRecebido",
                        recebido
                )
        );
        if (divergencia.signum() > 0) {
            StatusSolicitacaoNumerario anterior = status;
            alterarStatus(StatusSolicitacaoNumerario.COM_DIVERGENCIA);
            registrar(
                    EventoHistoricoSolicitacao.DIVERGENCIA_REGISTRADA,
                    anterior.name(),
                    status.name(),
                    gestor,
                    agora,
                    null,
                    Map.of(
                            "valorDivergencia",
                            divergencia
                    )
            );
        } else {
            concluir(
                    gestor,
                    agora
            );
        }
    }

    void registrarOcorrencia(Usuario gestor, String descricao, Instant agora) {
        registrar(
                EventoHistoricoSolicitacao.OCORRENCIA_REGISTRADA,
                status.name(),
                status.name(),
                gestor,
                agora,
                descricao,
                Map.of()
        );
    }

    void conciliar(Usuario gestor, String justificativa, Instant agora) {
        if (status != StatusSolicitacaoNumerario.COM_DIVERGENCIA) {
            throw new TransicaoStatusInvalidaException();
        }
        registrar(
                EventoHistoricoSolicitacao.DIVERGENCIA_CONCILIADA,
                status.name(),
                status.name(),
                gestor,
                agora,
                justificativa,
                Map.of()
        );
        concluir(
                gestor,
                agora
        );
    }

    private void concluir(Usuario gestor, Instant agora) {
        StatusSolicitacaoNumerario anterior = status;
        alterarStatus(StatusSolicitacaoNumerario.CONCLUIDA);
        dataConclusao = obrigatorio(agora);
        registrar(
                EventoHistoricoSolicitacao.SOLICITACAO_CONCLUIDA,
                anterior.name(),
                status.name(),
                gestor,
                agora,
                null,
                Map.of()
        );
    }

    private void validarAlteracao(StatusSolicitacaoNumerario esperado, Usuario gestor, long versaoEsperada) {
        validarVersaoEStatus(
                esperado,
                versaoEsperada
        );
        validarGestor(gestor);
    }

    private void validarVersaoEStatus(StatusSolicitacaoNumerario esperado, long versaoEsperada) {
        if (versao != versaoEsperada) {
            throw new ConflitoVersaoException();
        }
        if (status != esperado) {
            throw new TransicaoStatusInvalidaException();
        }
    }

    private void alterarStatus(StatusSolicitacaoNumerario novoStatus) {
        status = novoStatus;
    }

    private void validarGestorOuSolicitante(Usuario usuario) {
        if (usuario == null) {
            throw new ApenasGestorPodeDecidirException();
        }
        boolean solicitanteIgual = solicitante.getId() != null && solicitante.getId()
                .equals(usuario.getId());
        if (!solicitanteIgual && usuario.getPerfil() != PerfilUsuario.GESTOR) {
            throw new ApenasGestorPodeDecidirException();
        }
    }

    private static void validarGestor(Usuario usuario) {
        if (usuario == null || usuario.getPerfil() != PerfilUsuario.GESTOR) {
            throw new ApenasGestorPodeDecidirException();
        }
    }

    private static void validarUnidadeAtiva(UnidadeOperacional unidade) {
        if (unidade == null || !unidade.isAtivo()) {
            throw new RegraOperacaoNumerarioException();
        }
    }

    private static boolean mesmaUnidade(UnidadeOperacional a, UnidadeOperacional b) {
        return a == b || (a.getId() != null && a.getId()
                .equals(b.getId()));
    }

    private void registrar(
            EventoHistoricoSolicitacao evento,
            String anterior,
            String novo,
            Usuario usuario,
            Instant agora,
            String justificativa,
            Map<String, Object> dados) {
        eventosNovos.add(
                new HistoricoSolicitacaoNumerario(
                        null,
                        id,
                        null,
                        evento,
                        anterior,
                        novo,
                        usuario.getId(),
                        obrigatorio(agora),
                        justificativa,
                        dados
                )
        );
    }

    private static String justificativaObrigatoria(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new JustificativaObrigatoriaException();
        }
        return valor.trim();
    }

    private static String textoObrigatorio(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException();
        }
        return valor.trim();
    }

    private static String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static <T> T obrigatorio(T valor) {
        if (valor == null) {
            throw new CampoObrigatorioException();
        }
        return valor;
    }

    public Long getId() {
        return id;
    }

    public TipoOperacaoNumerario getTipoOperacao() {
        return tipoOperacao;
    }

    public Long getAgenciaReferenciaId() {
        return agenciaReferenciaId;
    }

    public UnidadeOperacional getOrigem() {
        return origem;
    }

    public UnidadeOperacional getDestino() {
        return destino;
    }

    public BigDecimal getValorSolicitado() {
        return valorSolicitado;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDate getDataDesejada() {
        return dataDesejada;
    }

    public StatusSolicitacaoNumerario getStatus() {
        return status;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public Usuario getAprovador() {
        return aprovador;
    }

    public String getJustificativaDecisao() {
        return justificativaDecisao;
    }

    public Instant getDataCriacao() {
        return dataCriacao;
    }

    public Instant getDataDecisao() {
        return dataDecisao;
    }

    public Usuario getCanceladoPor() {
        return canceladoPor;
    }

    public String getJustificativaCancelamento() {
        return justificativaCancelamento;
    }

    public Instant getDataCancelamento() {
        return dataCancelamento;
    }

    public Instant getDataConclusao() {
        return dataConclusao;
    }

    public long getVersao() {
        return versao;
    }

    public List<HistoricoSolicitacaoNumerario> getEventosNovos() {
        return List.copyOf(eventosNovos);
    }
}
