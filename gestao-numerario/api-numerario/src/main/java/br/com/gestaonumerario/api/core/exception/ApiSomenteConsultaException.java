package br.com.gestaonumerario.api.core.exception;

public class ApiSomenteConsultaException extends BaseException {
    public ApiSomenteConsultaException() {
        super(ErrorEnum.API_V1_SOMENTE_CONSULTA);
    }
}
