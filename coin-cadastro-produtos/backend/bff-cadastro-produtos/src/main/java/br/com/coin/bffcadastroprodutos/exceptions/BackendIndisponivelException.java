package br.com.coin.bffcadastroprodutos.exceptions;

import br.com.coin.bffcadastroprodutos.enums.BffErrorEnum;

public class BackendIndisponivelException extends BffException {

    public BackendIndisponivelException() {
        super(BffErrorEnum.BACKEND_INDISPONIVEL);
    }
}
