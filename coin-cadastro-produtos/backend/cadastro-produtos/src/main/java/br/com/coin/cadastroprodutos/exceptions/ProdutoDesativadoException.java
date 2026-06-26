package br.com.coin.cadastroprodutos.exceptions;

import static br.com.coin.cadastroprodutos.enums.ErrorEnum.PRODUTO_DESATIVADO;

public class ProdutoDesativadoException extends BaseException {
    public ProdutoDesativadoException() {
        super(PRODUTO_DESATIVADO);
    }
}
