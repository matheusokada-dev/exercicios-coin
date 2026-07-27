package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.enums.StatusOperacaoNumerario;
import br.com.gestaonumerario.api.core.exception.ApenasGestorPodeDecidirException;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.ConflitoVersaoException;
import br.com.gestaonumerario.api.core.exception.JustificativaObrigatoriaException;
import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import br.com.gestaonumerario.api.core.exception.TransicaoStatusInvalidaException;
import java.math.BigDecimal;
import java.time.Instant;

public class OperacaoNumerario {

    private final Long id;
    private final SolicitacaoNumerario solicitacao;
    private final UnidadeOperacional origem;
    private final UnidadeOperacional destino;
    private final BigDecimal valorProgramado;
    private final Usuario programadoPor;
    private final Instant dataProgramacao;
    private final String idempotencyKey;
    private StatusOperacaoNumerario status;
    private BigDecimal valorExpedido;
    private BigDecimal valorRecebido;
    private BigDecimal valorDivergencia;
    private Usuario expedidoPor;
    private Usuario recebidoPor;
    private Usuario conciliadoPor;
    private Instant dataExpedicao;
    private Instant dataRecebimento;
    private Instant dataConciliacao;
    private String justificativaDivergencia;
    private String descricaoOcorrencia;
    private long versao;

    private OperacaoNumerario(Long id, SolicitacaoNumerario solicitacao,
                             UnidadeOperacional origem, UnidadeOperacional destino,
                             BigDecimal valorProgramado, Usuario programadoPor,
                             Instant dataProgramacao, String idempotencyKey, long versao) {
        this.id = id;
        this.solicitacao = obrigatorio(solicitacao);
        this.origem = obrigatorio(origem);
        this.destino = obrigatorio(destino);
        this.valorProgramado = ValorMonetario.exigirPositivo(valorProgramado);
        this.programadoPor = obrigatorio(programadoPor);
        this.dataProgramacao = obrigatorio(dataProgramacao);
        this.idempotencyKey = textoObrigatorio(idempotencyKey);
        this.status = StatusOperacaoNumerario.PROGRAMADA;
        this.versao = versao;
    }

    static OperacaoNumerario programar(SolicitacaoNumerario solicitacao,
                                       UnidadeOperacional origem, UnidadeOperacional destino,
                                       BigDecimal valorProgramado, Usuario gestor,
                                       String idempotencyKey, Instant agora) {
        return new OperacaoNumerario(null, solicitacao, origem, destino,
                valorProgramado, gestor(gestor), agora, idempotencyKey, 0);
    }

    public static OperacaoNumerario reconstituir(
            Long id, SolicitacaoNumerario solicitacao,
            UnidadeOperacional origem, UnidadeOperacional destino,
            StatusOperacaoNumerario status, BigDecimal valorProgramado,
            BigDecimal valorExpedido, BigDecimal valorRecebido,
            BigDecimal valorDivergencia, Usuario programadoPor,
            Usuario expedidoPor, Usuario recebidoPor, Usuario conciliadoPor,
            Instant dataProgramacao, Instant dataExpedicao,
            Instant dataRecebimento, Instant dataConciliacao,
            String justificativaDivergencia, String descricaoOcorrencia,
            String idempotencyKey, long versao
    ) {
        OperacaoNumerario operacao = new OperacaoNumerario(
                id, solicitacao, origem, destino, valorProgramado,
                programadoPor, dataProgramacao, idempotencyKey, versao);
        operacao.status = obrigatorio(status);
        operacao.valorExpedido = valorExpedido;
        operacao.valorRecebido = valorRecebido;
        operacao.valorDivergencia = valorDivergencia;
        operacao.expedidoPor = expedidoPor;
        operacao.recebidoPor = recebidoPor;
        operacao.conciliadoPor = conciliadoPor;
        operacao.dataExpedicao = dataExpedicao;
        operacao.dataRecebimento = dataRecebimento;
        operacao.dataConciliacao = dataConciliacao;
        operacao.justificativaDivergencia = textoOpcional(justificativaDivergencia);
        operacao.descricaoOcorrencia = textoOpcional(descricaoOcorrencia);
        return operacao;
    }

    public void iniciarSeparacao(Usuario usuario, long versaoEsperada, Instant agora) {
        validarVersaoEStatus(versaoEsperada, StatusOperacaoNumerario.PROGRAMADA);
        gestor(usuario);
        obrigatorio(agora);
        status = StatusOperacaoNumerario.EM_SEPARACAO;
        solicitacao.registrarSeparacao(usuario, agora);
    }

    public void expedir(Usuario usuario, long versaoEsperada, long versaoOrigem,
                        String chaveIdempotencia, Instant agora) {
        validarVersao(versaoEsperada);
        if (status != StatusOperacaoNumerario.PROGRAMADA
                && status != StatusOperacaoNumerario.EM_SEPARACAO) {
            throw new TransicaoStatusInvalidaException();
        }
        Usuario gestor = gestor(usuario);
        validarChave(chaveIdempotencia);
        origem.debitar(valorProgramado, versaoOrigem, agora);
        valorExpedido = valorProgramado;
        expedidoPor = gestor;
        dataExpedicao = obrigatorio(agora);
        status = StatusOperacaoNumerario.EM_TRANSITO;
        solicitacao.registrarExpedicao(gestor, agora);
    }

    public void receber(BigDecimal valor, String justificativa, Usuario usuario,
                        long versaoEsperada, long versaoDestino,
                        String chaveIdempotencia, Instant agora) {
        validarVersaoEStatus(versaoEsperada, StatusOperacaoNumerario.EM_TRANSITO);
        Usuario gestor = gestor(usuario);
        validarChave(chaveIdempotencia);
        BigDecimal recebido = ValorMonetario.exigirPositivo(valor);
        if (recebido.compareTo(valorExpedido) > 0) {
            throw new RegraOperacaoNumerarioException();
        }
        BigDecimal divergencia = valorExpedido.subtract(recebido);
        String justificativaValidada = divergencia.signum() > 0
                ? justificativaObrigatoria(justificativa) : textoOpcional(justificativa);
        destino.creditar(recebido, versaoDestino, agora);
        valorRecebido = recebido;
        valorDivergencia = divergencia;
        justificativaDivergencia = justificativaValidada;
        recebidoPor = gestor;
        dataRecebimento = obrigatorio(agora);
        status = divergencia.signum() > 0
                ? StatusOperacaoNumerario.COM_DIVERGENCIA
                : StatusOperacaoNumerario.RECEBIDA;
        solicitacao.registrarRecebimento(gestor, recebido, divergencia, agora);
    }

    public void conciliar(String justificativa, Usuario usuario, long versaoEsperada,
                          String chaveIdempotencia, Instant agora) {
        validarVersaoEStatus(versaoEsperada, StatusOperacaoNumerario.COM_DIVERGENCIA);
        Usuario gestor = gestor(usuario);
        validarChave(chaveIdempotencia);
        String texto = justificativaObrigatoria(justificativa);
        status = StatusOperacaoNumerario.CONCILIADA;
        conciliadoPor = gestor;
        dataConciliacao = obrigatorio(agora);
        solicitacao.conciliar(gestor, texto, agora);
    }

    public void registrarOcorrencia(String descricao, Usuario usuario,
                                    long versaoEsperada, Instant agora) {
        validarVersao(versaoEsperada);
        if (status != StatusOperacaoNumerario.PROGRAMADA
                && status != StatusOperacaoNumerario.EM_SEPARACAO
                && status != StatusOperacaoNumerario.EM_TRANSITO) {
            throw new TransicaoStatusInvalidaException();
        }
        Usuario gestor = gestor(usuario);
        descricaoOcorrencia = justificativaObrigatoria(descricao);
        obrigatorio(agora);
        solicitacao.registrarOcorrencia(gestor, descricaoOcorrencia, agora);
    }

    private static void validarChave(String chave) {
        textoObrigatorio(chave);
    }

    private void validarVersaoEStatus(long esperada, StatusOperacaoNumerario esperado) {
        validarVersao(esperada);
        if (status != esperado) {
            throw new TransicaoStatusInvalidaException();
        }
    }

    private void validarVersao(long esperada) {
        if (versao != esperada) {
            throw new ConflitoVersaoException();
        }
    }

    private static Usuario gestor(Usuario usuario) {
        if (usuario == null || usuario.getPerfil() != PerfilUsuario.GESTOR) {
            throw new ApenasGestorPodeDecidirException();
        }
        return usuario;
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

    public Long getId() { return id; }
    public SolicitacaoNumerario getSolicitacao() { return solicitacao; }
    public UnidadeOperacional getOrigem() { return origem; }
    public UnidadeOperacional getDestino() { return destino; }
    public StatusOperacaoNumerario getStatus() { return status; }
    public BigDecimal getValorProgramado() { return valorProgramado; }
    public BigDecimal getValorExpedido() { return valorExpedido; }
    public BigDecimal getValorRecebido() { return valorRecebido; }
    public BigDecimal getValorDivergencia() { return valorDivergencia; }
    public Usuario getProgramadoPor() { return programadoPor; }
    public Usuario getExpedidoPor() { return expedidoPor; }
    public Usuario getRecebidoPor() { return recebidoPor; }
    public Usuario getConciliadoPor() { return conciliadoPor; }
    public Instant getDataProgramacao() { return dataProgramacao; }
    public Instant getDataExpedicao() { return dataExpedicao; }
    public Instant getDataRecebimento() { return dataRecebimento; }
    public Instant getDataConciliacao() { return dataConciliacao; }
    public String getJustificativaDivergencia() { return justificativaDivergencia; }
    public String getDescricaoOcorrencia() { return descricaoOcorrencia; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public long getVersao() { return versao; }
}
