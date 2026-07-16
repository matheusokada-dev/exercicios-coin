package br.com.coin.bffcadastroprodutos.enums;

import lombok.Getter;

@Getter
public enum BffErrorEnum {
    ERRO_GENERICO(500, 1, "Erro interno na BFF."),
    BACKEND_INDISPONIVEL(503, 503, "Serviço de produtos indisponível."),
    BACKEND_TIMEOUT(504, 504, "Tempo limite excedido ao consultar o serviço de produtos."),
    REQUISICAO_INVALIDA(400, 400, "Request inválido.");

    private final int httpStatus;
    private final int errorCode;
    private final String errorMessage;

    BffErrorEnum(int httpStatus, int errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
