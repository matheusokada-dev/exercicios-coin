package br.com.gestaonumerario.api.core.exception;

public class TipoMovimentacaoNaoPermitidoException extends BaseException {

    public TipoMovimentacaoNaoPermitidoException() {
        super(ErrorEnum.TIPO_MOVIMENTACAO_NAO_PERMITIDO);
    }
}
