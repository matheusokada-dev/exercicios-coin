package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.SaldoInsuficienteException;
import java.math.BigDecimal;
import java.time.Instant;

public class Movimentacao {

    private final Long id;
    private final Agencia agencia;
    private final SolicitacaoAbastecimento solicitacao;
    private final TipoMovimentacao tipo;
    private final boolean entrada;
    private final BigDecimal valor;
    private final BigDecimal saldoAnterior;
    private final BigDecimal saldoPosterior;
    private final String descricao;
    private final Instant dataMovimento;
    private final Usuario usuario;
    private final String idempotencyKey;

    private Movimentacao(
            Long id,
            Agencia agencia,
            SolicitacaoAbastecimento solicitacao,
            TipoMovimentacao tipo,
            boolean entrada,
            BigDecimal valor,
            BigDecimal saldoAnterior,
            BigDecimal saldoPosterior,
            String descricao,
            Instant dataMovimento,
            Usuario usuario,
            String idempotencyKey
    ) {
        this.id = id;
        this.agencia = obrigatorio(agencia);
        this.solicitacao = solicitacao;
        this.tipo = obrigatorio(tipo);
        this.entrada = entrada;
        this.valor = ValorMonetario.exigirPositivo(valor);
        this.saldoAnterior = ValorMonetario.exigirNaoNegativo(saldoAnterior);
        this.saldoPosterior = ValorMonetario.exigirNaoNegativo(saldoPosterior);
        this.descricao = textoOpcional(descricao);
        this.dataMovimento = obrigatorio(dataMovimento);
        this.usuario = obrigatorio(usuario);
        this.idempotencyKey = textoOpcional(idempotencyKey);
    }

    public static Movimentacao criar(
            Agencia agencia,
            SolicitacaoAbastecimento solicitacao,
            TipoMovimentacao tipo,
            boolean entrada,
            BigDecimal valor,
            String descricao,
            Instant dataMovimento,
            Usuario usuario,
            String idempotencyKey
    ) {
        BigDecimal valorNormalizado = ValorMonetario.exigirPositivo(valor);
        BigDecimal saldoAnterior = agencia.getSaldoAtual();
        BigDecimal saldoPosterior = calcularSaldoPosterior(
                entrada,
                saldoAnterior,
                valorNormalizado
        );

        return new Movimentacao(
                null,
                agencia,
                solicitacao,
                tipo,
                entrada,
                valorNormalizado,
                saldoAnterior,
                saldoPosterior,
                descricao,
                dataMovimento,
                usuario,
                idempotencyKey
        );
    }

    public static Movimentacao criar(
            Agencia agencia,
            SolicitacaoAbastecimento solicitacao,
            TipoMovimentacao tipo,
            BigDecimal valor,
            String descricao,
            Instant dataMovimento,
            Usuario usuario,
            String idempotencyKey
    ) {
        Boolean entradaPadrao = obrigatorio(tipo).getEntradaPadrao();
        if (entradaPadrao == null) {
            throw new CampoObrigatorioException();
        }
        return criar(agencia, solicitacao, tipo, entradaPadrao, valor, descricao, dataMovimento, usuario, idempotencyKey);
    }

    public static Movimentacao reconstituir(
            Long id,
            Agencia agencia,
            SolicitacaoAbastecimento solicitacao,
            TipoMovimentacao tipo,
            boolean entrada,
            BigDecimal valor,
            BigDecimal saldoAnterior,
            BigDecimal saldoPosterior,
            String descricao,
            Instant dataMovimento,
            Usuario usuario,
            String idempotencyKey
    ) {
        return new Movimentacao(
                id,
                agencia,
                solicitacao,
                tipo,
                entrada,
                valor,
                saldoAnterior,
                saldoPosterior,
                descricao,
                dataMovimento,
                usuario,
                idempotencyKey
        );
    }

    private static BigDecimal calcularSaldoPosterior(
            boolean entrada,
            BigDecimal saldoAnterior,
            BigDecimal valor
    ) {
        BigDecimal saldo = ValorMonetario.exigirNaoNegativo(saldoAnterior);

        BigDecimal saldoPosterior = entrada
                ? saldo.add(valor)
                : saldo.subtract(valor);

        if (saldoPosterior.signum() < 0) {
            throw new SaldoInsuficienteException();
        }

        return saldoPosterior;
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
    public Agencia getAgencia() { return agencia; }
    public SolicitacaoAbastecimento getSolicitacao() { return solicitacao; }
    public TipoMovimentacao getTipo() { return tipo; }
    public boolean isEntrada() { return entrada; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getSaldoAnterior() { return saldoAnterior; }
    public BigDecimal getSaldoPosterior() { return saldoPosterior; }
    public String getDescricao() { return descricao; }
    public Instant getDataMovimento() { return dataMovimento; }
    public Usuario getUsuario() { return usuario; }
    public String getIdempotencyKey() { return idempotencyKey; }
}
