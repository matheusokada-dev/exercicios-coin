package br.com.gestaonumerario.api.core.exception;

public class DataDesejadaNoPassadoException extends BaseException {

    public DataDesejadaNoPassadoException() {
        super(ErrorEnum.DATA_DESEJADA_NO_PASSADO);
    }
}
