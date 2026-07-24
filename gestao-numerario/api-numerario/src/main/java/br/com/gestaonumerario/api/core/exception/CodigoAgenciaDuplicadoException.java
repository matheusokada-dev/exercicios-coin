package br.com.gestaonumerario.api.core.exception;

public class CodigoAgenciaDuplicadoException extends BaseException {

    public CodigoAgenciaDuplicadoException() {
        super(ErrorEnum.CODIGO_AGENCIA_JA_CADASTRADO);
    }
}
