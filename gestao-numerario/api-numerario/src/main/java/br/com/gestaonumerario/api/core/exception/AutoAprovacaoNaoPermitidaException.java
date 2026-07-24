package br.com.gestaonumerario.api.core.exception;

public class AutoAprovacaoNaoPermitidaException extends BaseException {

    public AutoAprovacaoNaoPermitidaException() {
        super(ErrorEnum.AUTO_APROVACAO_NAO_PERMITIDA);
    }
}