package br.com.gestaonumerario.api.core.exception;

public class AgenciaNaoEncontradaException extends BaseException {

    public AgenciaNaoEncontradaException() {
        super(ErrorEnum.AGENCIA_NAO_ENCONTRADA);
    }
}