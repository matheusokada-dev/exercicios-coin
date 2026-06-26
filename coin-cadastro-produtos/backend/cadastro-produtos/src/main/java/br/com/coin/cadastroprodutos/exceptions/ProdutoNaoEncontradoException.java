package br.com.coin.cadastroprodutos.exceptions;

import static br.com.coin.cadastroprodutos.enums.ErrorEnum.PRODUTO_NAO_EXISTENTE;

public class ProdutoNaoEncontradoException extends BaseException {
    public ProdutoNaoEncontradoException() {
        super(PRODUTO_NAO_EXISTENTE);
    }
}
