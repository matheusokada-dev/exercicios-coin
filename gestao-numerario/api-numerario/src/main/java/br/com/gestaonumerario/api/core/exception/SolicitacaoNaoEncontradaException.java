package br.com.gestaonumerario.api.core.exception;

public class SolicitacaoNaoEncontradaException extends BaseException {
    public SolicitacaoNaoEncontradaException() {
        super(ErrorEnum.SOLICITACAO_NAO_ENCONTRADA);
    }
}
