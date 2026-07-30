package br.com.gestaonumerario.api.core.exception;

public class JustificativaObrigatoriaException extends BaseException {

    public JustificativaObrigatoriaException() {
        super(ErrorEnum.JUSTIFICATIVA_OBRIGATORIA);
    }
}
