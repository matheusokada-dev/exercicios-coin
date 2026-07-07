package br.com.coin.bffcadastroprodutos.exceptions;

import br.com.coin.bffcadastroprodutos.enums.BffErrorEnum;

public class ProdutoBffValidationException extends BffException {

    public ProdutoBffValidationException(String message) {
        super(BffErrorEnum.REQUISICAO_INVALIDA, message);
    }
}
