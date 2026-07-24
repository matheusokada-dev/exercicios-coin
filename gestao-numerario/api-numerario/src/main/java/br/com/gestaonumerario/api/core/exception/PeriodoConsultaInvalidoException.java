package br.com.gestaonumerario.api.core.exception;

public class PeriodoConsultaInvalidoException extends BaseException {

    public PeriodoConsultaInvalidoException() {
        super(ErrorEnum.PERIODO_CONSULTA_INVALIDO);
    }
}
