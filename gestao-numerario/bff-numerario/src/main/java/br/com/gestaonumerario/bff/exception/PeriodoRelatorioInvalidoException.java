package br.com.gestaonumerario.bff.exception;

public class PeriodoRelatorioInvalidoException extends RuntimeException {

    public PeriodoRelatorioInvalidoException() {
        super("A data final não pode ser anterior à data inicial.");
    }
}
