package br.com.gestaonumerario.api.core.exception;

public class SolicitacaoAbertaDuplicadaException extends BaseException {
    public SolicitacaoAbertaDuplicadaException() {
        super(ErrorEnum.SOLICITACAO_ABERTA_DUPLICADA);
    }
}
