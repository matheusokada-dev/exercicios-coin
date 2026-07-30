package br.com.gestaonumerario.bff.exception;

public class RelatorioSemDadosException extends RuntimeException {

    public RelatorioSemDadosException() {
        super("Não há movimentações para a agência e o período informados.");
    }
}
