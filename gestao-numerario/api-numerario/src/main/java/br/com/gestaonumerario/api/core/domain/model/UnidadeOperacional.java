package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.ConflitoVersaoException;
import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import br.com.gestaonumerario.api.core.exception.SaldoInsuficienteException;
import java.math.BigDecimal;
import java.time.Instant;

public class UnidadeOperacional {

    private final Long id;
    private final TipoUnidadeOperacional tipo;
    private final String codigo;
    private final String nome;
    private final boolean controlaSaldo;
    private final Instant criadoEm;
    private BigDecimal saldoAtual;
    private boolean ativo;
    private long versao;
    private Instant atualizadoEm;

    public UnidadeOperacional(
            Long id,
            TipoUnidadeOperacional tipo,
            String codigo,
            String nome,
            boolean controlaSaldo,
            BigDecimal saldoAtual,
            boolean ativo,
            long versao,
            Instant criadoEm,
            Instant atualizadoEm) {
        this.id = id;
        this.tipo = obrigatorio(
                tipo,
                "tipo"
        );
        this.codigo = textoObrigatorio(
                codigo,
                "codigo"
        );
        this.nome = textoObrigatorio(
                nome,
                "nome"
        );
        this.controlaSaldo = controlaSaldo;
        this.saldoAtual = ValorMonetario.exigirNaoNegativo(saldoAtual);
        if (!controlaSaldo && this.saldoAtual.signum() != 0) {
            throw new RegraOperacaoNumerarioException(
                    "Uma unidade que não controla saldo deve possuir saldo igual a zero."
            );
        }
        this.ativo = ativo;
        this.versao = versao;
        this.criadoEm = obrigatorio(
                criadoEm,
                "criadoEm"
        );
        this.atualizadoEm = obrigatorio(
                atualizadoEm,
                "atualizadoEm"
        );
    }

    public void debitar(BigDecimal valor, long versaoEsperada, Instant agora) {
        validarMovimentavel(versaoEsperada);
        BigDecimal debito = ValorMonetario.exigirPositivo(valor);
        if (saldoAtual.compareTo(debito) < 0) {
            throw new SaldoInsuficienteException();
        }
        saldoAtual = saldoAtual.subtract(debito);
        avancarVersao(agora);
    }

    public void creditar(BigDecimal valor, long versaoEsperada, Instant agora) {
        validarMovimentavel(versaoEsperada);
        saldoAtual = saldoAtual.add(ValorMonetario.exigirPositivo(valor));
        avancarVersao(agora);
    }

    public void ajustar(BigDecimal valor, boolean entrada, long versaoEsperada, Instant agora) {
        if (entrada) {
            creditar(
                    valor,
                    versaoEsperada,
                    agora
            );
        } else {
            debitar(
                    valor,
                    versaoEsperada,
                    agora
            );
        }
    }

    private void validarMovimentavel(long versaoEsperada) {
        if (!ativo || !controlaSaldo) {
            throw new RegraOperacaoNumerarioException(
                    !ativo
                            ? "A unidade operacional está inativa."
                            : "A unidade operacional não permite movimentação de saldo."
            );
        }
        if (versao != versaoEsperada) {
            throw new ConflitoVersaoException();
        }
    }

    private void avancarVersao(Instant agora) {
        atualizadoEm = obrigatorio(
                agora,
                "agora"
        );
    }

    private static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException(campo);
        }
        return valor.trim();
    }

    private static <T> T obrigatorio(T valor, String campo) {
        if (valor == null) {
            throw new CampoObrigatorioException(campo);
        }
        return valor;
    }

    public Long getId() {
        return id;
    }

    public TipoUnidadeOperacional getTipo() {
        return tipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public boolean isControlaSaldo() {
        return controlaSaldo;
    }

    public BigDecimal getSaldoAtual() {
        return saldoAtual;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public long getVersao() {
        return versao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
