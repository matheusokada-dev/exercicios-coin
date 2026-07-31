package br.com.gestaonumerario.api.core.exception;

public class ValorDeveSerPositivoException extends BaseException {

    public ValorDeveSerPositivoException() {
        super(ErrorEnum.VALOR_DEVE_SER_MAIOR_QUE_ZERO);
    }
}
