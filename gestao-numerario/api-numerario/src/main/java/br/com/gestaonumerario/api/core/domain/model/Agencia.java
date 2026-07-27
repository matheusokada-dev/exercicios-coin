package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import br.com.gestaonumerario.api.core.exception.SaldoInsuficienteException;
import java.math.BigDecimal;

public class Agencia {

    private final Long id;
    private final String codigo;
    private String nome;
    private String cidade;
    private BigDecimal saldoAtual;
    private BigDecimal limiteMinimo;
    private boolean ativo;
    private long versao;

    public Agencia(
            Long id,
            String codigo,
            String nome,
            String cidade,
            BigDecimal saldoAtual,
            BigDecimal limiteMinimo,
            boolean ativo,
            long versao
    ) {
        this.id = id;
        this.codigo = textoObrigatorio(codigo, "codigo");
        this.nome = textoObrigatorio(nome, "nome");
        this.cidade = textoObrigatorio(cidade, "cidade");
        this.saldoAtual = ValorMonetario.exigirNaoNegativo(saldoAtual);
        this.limiteMinimo = ValorMonetario.exigirNaoNegativo(limiteMinimo);
        this.ativo = ativo;
        this.versao = versao;
    }

    public boolean estaAbaixoDoLimite() {
        return saldoAtual.compareTo(limiteMinimo) < 0;
    }

    public BigDecimal calcularSugestaoAbastecimento() {
        if (!estaAbaixoDoLimite()) {
            return ValorMonetario.zero();
        }

        return limiteMinimo.subtract(saldoAtual);
    }

    public void abastecer(BigDecimal valor) {
        exigirAtiva();
        saldoAtual = saldoAtual.add(ValorMonetario.exigirPositivo(valor));
    }

    public void retirar(BigDecimal valor) {
        exigirAtiva();
        BigDecimal valorRetirada = ValorMonetario.exigirPositivo(valor);

        if (saldoAtual.compareTo(valorRetirada) < 0) {
            throw new SaldoInsuficienteException();
        }

        saldoAtual = saldoAtual.subtract(valorRetirada);
    }

    public void atualizarDados(String nome, String cidade, BigDecimal limiteMinimo) {
        exigirAtiva();
        this.nome = textoObrigatorio(nome, "nome");
        this.cidade = textoObrigatorio(cidade, "cidade");
        this.limiteMinimo = ValorMonetario.exigirNaoNegativo(limiteMinimo);
    }

    public void ativar() {
        ativo = true;
    }

    public void desativar() {
        ativo = false;
    }

    public void exigirAtiva() {
        if (!ativo) {
            throw new RegraOperacaoNumerarioException(
                    "A agência está inativa e não pode participar desta operação.");
        }
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getCidade() { return cidade; }
    public BigDecimal getSaldoAtual() { return saldoAtual; }
    public BigDecimal getLimiteMinimo() { return limiteMinimo; }
    public boolean isAtivo() { return ativo; }
    public long getVersao() { return versao; }

    private static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException(campo);
        }

        return valor.trim();
    }
}
