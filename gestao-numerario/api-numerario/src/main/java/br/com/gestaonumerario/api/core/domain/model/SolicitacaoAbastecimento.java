package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacao;
import br.com.gestaonumerario.api.core.exception.ApenasGestorPodeDecidirException;
import br.com.gestaonumerario.api.core.exception.AutoAprovacaoNaoPermitidaException;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.DataDesejadaNoPassadoException;
import br.com.gestaonumerario.api.core.exception.JustificativaEspecialObrigatoriaException;
import br.com.gestaonumerario.api.core.exception.JustificativaObrigatoriaException;
import br.com.gestaonumerario.api.core.exception.TransicaoStatusInvalidaException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class SolicitacaoAbastecimento {

    private static final BigDecimal LIMITE_JUSTIFICATIVA_ESPECIAL = new BigDecimal("500000.00");

    private final Long id;
    private final Agencia agencia;
    private final BigDecimal valor;
    private final String motivo;
    private final LocalDate dataDesejada;
    private final Usuario solicitante;
    private final Instant dataCriacao;
    private final long versao;

    private StatusSolicitacao status;
    private Usuario decisor;
    private String justificativaDecisao;
    private String justificativaEspecial;
    private Instant dataDecisao;
    private Instant dataAtendimento;

    private SolicitacaoAbastecimento(
            Long id,
            Agencia agencia,
            BigDecimal valor,
            String motivo,
            LocalDate dataDesejada,
            StatusSolicitacao status,
            Usuario solicitante,
            Usuario decisor,
            String justificativaDecisao,
            String justificativaEspecial,
            Instant dataCriacao,
            Instant dataDecisao,
            Instant dataAtendimento,
            long versao) {
        this.id = id;
        this.agencia = obrigatorio(agencia);
        this.valor = ValorMonetario.exigirPositivo(valor);
        this.motivo = justificativaObrigatoria(motivo);
        this.dataDesejada = obrigatorio(dataDesejada);
        this.status = obrigatorio(status);
        this.solicitante = obrigatorio(solicitante);
        this.decisor = decisor;
        this.justificativaDecisao = textoOpcional(justificativaDecisao);
        this.justificativaEspecial = textoOpcional(justificativaEspecial);
        this.dataCriacao = obrigatorio(dataCriacao);
        this.dataDecisao = dataDecisao;
        this.dataAtendimento = dataAtendimento;
        this.versao = versao;
    }

    public static SolicitacaoAbastecimento criar(
            Agencia agencia,
            BigDecimal valor,
            String motivo,
            LocalDate dataDesejada,
            Usuario solicitante,
            LocalDate dataReferencia,
            Instant agora) {
        if (dataReferencia == null || agora == null) {
            throw new CampoObrigatorioException();
        }

        if (dataDesejada == null || dataDesejada.isBefore(dataReferencia)) {
            throw new DataDesejadaNoPassadoException();
        }

        return new SolicitacaoAbastecimento(
                null,
                agencia,
                valor,
                motivo,
                dataDesejada,
                StatusSolicitacao.PENDENTE,
                solicitante,
                null,
                null,
                null,
                agora,
                null,
                null,
                0
        );
    }

    public static SolicitacaoAbastecimento reconstituir(
            Long id,
            Agencia agencia,
            BigDecimal valor,
            String motivo,
            LocalDate dataDesejada,
            StatusSolicitacao status,
            Usuario solicitante,
            Usuario decisor,
            String justificativaDecisao,
            String justificativaEspecial,
            Instant dataCriacao,
            Instant dataDecisao,
            Instant dataAtendimento,
            long versao) {
        return new SolicitacaoAbastecimento(
                id,
                agencia,
                valor,
                motivo,
                dataDesejada,
                status,
                solicitante,
                decisor,
                justificativaDecisao,
                justificativaEspecial,
                dataCriacao,
                dataDecisao,
                dataAtendimento,
                versao
        );
    }

    public void aprovar(Usuario decisor, String justificativaDecisao, String justificativaEspecial, Instant agora) {
        validarStatus(StatusSolicitacao.PENDENTE);
        validarGestor(decisor);
        validarNaoAutoAprovacao(decisor);

        String decisao = justificativaObrigatoria(justificativaDecisao);
        String especial = textoOpcional(justificativaEspecial);

        if (valor.compareTo(LIMITE_JUSTIFICATIVA_ESPECIAL) > 0 && especial == null) {
            throw new JustificativaEspecialObrigatoriaException();
        }

        this.status = StatusSolicitacao.APROVADA;
        this.decisor = decisor;
        this.justificativaDecisao = decisao;
        this.justificativaEspecial = especial;
        this.dataDecisao = obrigatorio(agora);
    }

    public void rejeitar(Usuario decisor, String justificativaDecisao, Instant agora) {
        validarStatus(StatusSolicitacao.PENDENTE);
        validarGestor(decisor);

        this.status = StatusSolicitacao.REJEITADA;
        this.decisor = decisor;
        this.justificativaDecisao = justificativaObrigatoria(justificativaDecisao);
        this.dataDecisao = obrigatorio(agora);
    }

    public void atender(Instant agora) {
        validarStatus(StatusSolicitacao.APROVADA);

        this.status = StatusSolicitacao.ATENDIDA;
        this.dataAtendimento = obrigatorio(agora);
    }

    private void validarStatus(StatusSolicitacao statusEsperado) {
        if (status != statusEsperado) {
            throw new TransicaoStatusInvalidaException();
        }
    }

    private void validarGestor(Usuario decisor) {
        if (decisor == null || decisor.getPerfil() != PerfilUsuario.GESTOR) {
            throw new ApenasGestorPodeDecidirException();
        }
    }

    private void validarNaoAutoAprovacao(Usuario decisor) {
        Long idSolicitante = solicitante.getId();
        Long idDecisor = decisor.getId();

        boolean mesmoUsuario =
                idSolicitante != null && idDecisor != null ? idSolicitante.equals(idDecisor) : solicitante == decisor;

        if (mesmoUsuario) {
            throw new AutoAprovacaoNaoPermitidaException();
        }
    }

    private static String justificativaObrigatoria(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new JustificativaObrigatoriaException();
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

    public Agencia getAgencia() {
        return agencia;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDate getDataDesejada() {
        return dataDesejada;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public Instant getDataCriacao() {
        return dataCriacao;
    }

    public long getVersao() {
        return versao;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public Usuario getDecisor() {
        return decisor;
    }

    public String getJustificativaDecisao() {
        return justificativaDecisao;
    }

    public String getJustificativaEspecial() {
        return justificativaEspecial;
    }

    public Instant getDataDecisao() {
        return dataDecisao;
    }

    public Instant getDataAtendimento() {
        return dataAtendimento;
    }
}
