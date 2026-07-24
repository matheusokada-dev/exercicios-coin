package br.com.gestaonumerario.api.core.exception;

public class TransicaoStatusInvalidaException extends BaseException {

    public TransicaoStatusInvalidaException() {
        super(ErrorEnum.TRANSICAO_STATUS_INVALIDA);
    }

}