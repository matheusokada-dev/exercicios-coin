package br.com.gestaonumerario.api.core.exception;

public class ConflitoVersaoException extends BaseException {
    public ConflitoVersaoException() {
        super(ErrorEnum.CONFLITO_VERSAO);
    }
}
