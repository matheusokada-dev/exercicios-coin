package br.com.coin.bffcadastroprodutos.exceptions;

import br.com.coin.bffcadastroprodutos.enums.BffErrorEnum;

public class BackendTimeoutException extends BffException {

    public BackendTimeoutException() {
        super(BffErrorEnum.BACKEND_TIMEOUT);
    }
}
