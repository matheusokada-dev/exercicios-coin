package br.com.gestaonumerario.api.core.exception;

public class CampoObrigatorioException extends BaseException {

    public CampoObrigatorioException() {
        super(ErrorEnum.CAMPO_OBRIGATORIO);
    }
}