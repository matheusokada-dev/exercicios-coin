package br.com.gestaonumerario.api.core.exception;

public class RegraOperacaoNumerarioException extends BaseException {
    public RegraOperacaoNumerarioException() {
        super(ErrorEnum.REGRA_OPERACAO_NUMERARIO_VIOLADA);
    }

    public RegraOperacaoNumerarioException(String motivo) {
        super(ErrorEnum.REGRA_OPERACAO_NUMERARIO_VIOLADA, motivo);
    }
}
