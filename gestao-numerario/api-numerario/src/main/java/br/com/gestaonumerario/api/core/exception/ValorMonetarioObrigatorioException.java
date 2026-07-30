package br.com.gestaonumerario.api.core.exception;

public class ValorMonetarioObrigatorioException extends BaseException {

    public ValorMonetarioObrigatorioException() {
        super(ErrorEnum.VALOR_MONETARIO_OBRIGATORIO);
    }
}
