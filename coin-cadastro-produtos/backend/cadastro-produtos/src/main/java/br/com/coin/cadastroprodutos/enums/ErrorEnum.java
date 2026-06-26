package br.com.coin.cadastroprodutos.enums;

import lombok.Getter;

@Getter
public enum ErrorEnum {
    ERRO_GENERICO(500, 1, "Algo deu errado, tente novamente mais tarde."),
    PRODUTO_NAO_EXISTENTE(404, 1000, "Produto não existente."),
    PRODUTO_DESATIVADO(404, 1001, "Produto já desativado.");

    private final int httpStatus;
    private final int errorCode;
    private final String errorMessage;

    ErrorEnum(int httpStatus, int errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
