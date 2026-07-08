package br.com.coin.bffcadastroprodutos.enums;

import lombok.Getter;

@Getter
public enum BffErrorEnum {
    ERRO_GENERICO(500, 1, "Erro interno na BFF."),
    BACKEND_INDISPONIVEL(503, 503, "Servi\u00e7o de produtos indispon\u00edvel."),
    BACKEND_TIMEOUT(504, 504, "Tempo limite excedido ao consultar o servi\u00e7o de produtos."),
    REQUISICAO_INVALIDA(400, 400, "Request inv\u00e1lido.");
    //Adicionar 0 404
    private final int httpStatus;
    private final int errorCode;
    private final String errorMessage;

    BffErrorEnum(int httpStatus, int errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
