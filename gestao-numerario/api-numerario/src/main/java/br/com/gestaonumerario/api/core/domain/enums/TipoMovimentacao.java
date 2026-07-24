package br.com.gestaonumerario.api.core.domain.enums;

public enum TipoMovimentacao {
    ABASTECIMENTO(true),
    DEPOSITO(true),
    RECOLHIMENTO(false),
    SAQUE(false),
    AJUSTE(null);

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
