package br.com.gestaonumerario.api.core.exception;

public class ValorNaoPodeSerNegativoException extends BaseException {

    public ValorNaoPodeSerNegativoException() {
        super(ErrorEnum.VALOR_NAO_PODE_SER_NEGATIVO);
    }
}
