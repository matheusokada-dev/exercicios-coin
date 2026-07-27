package br.com.gestaonumerario.api.core.domain.enums;

public enum CampoOrdenacaoAgencia {
    CODIGO("codigo"),
    NOME("nome"),
    CIDADE("cidade"),
    SALDO_ATUAL("unidadeOperacional.saldoAtual"),
    LIMITE_MINIMO("limiteMinimo");

    private final String propriedadeJpa;

    CampoOrdenacaoAgencia(String propriedadeJpa) {
        this.propriedadeJpa = propriedadeJpa;
    }

    public String getPropriedadeJpa() {
        return propriedadeJpa;
    }
}
