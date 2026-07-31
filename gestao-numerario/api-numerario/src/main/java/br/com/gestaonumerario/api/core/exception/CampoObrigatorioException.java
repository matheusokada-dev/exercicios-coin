package br.com.gestaonumerario.api.core.exception;

public class CampoObrigatorioException extends BaseException {

    public CampoObrigatorioException() {
        super(ErrorEnum.CAMPO_OBRIGATORIO);
    }

    public CampoObrigatorioException(String campo) {
        super(ErrorEnum.CAMPO_OBRIGATORIO, "O campo '" + campo + "' é obrigatório.", campo);
    }
}
