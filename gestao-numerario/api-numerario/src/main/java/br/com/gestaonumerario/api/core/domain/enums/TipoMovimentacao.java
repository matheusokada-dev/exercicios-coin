package br.com.gestaonumerario.api.core.domain.enums;

public enum TipoMovimentacao {
    ABASTECIMENTO(true),
    DEPOSITO(true),
    RECOLHIMENTO(false),
    SAQUE(false),
    AJUSTE(null),
    SAIDA_PARA_TRANSITO(false),
    ENTRADA_DE_TRANSITO(true),
    AJUSTE_DIVERGENCIA(null);

    private final Boolean entradaPadrao;

    TipoMovimentacao(Boolean entradaPadrao) {
        this.entradaPadrao = entradaPadrao;
    }

    public Boolean getEntradaPadrao() {
        return entradaPadrao;
    }

    public boolean exigeDirecaoInformada() {
        return entradaPadrao == null;
    }
}
