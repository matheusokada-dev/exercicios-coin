package br.com.coin.bffcadastroprodutos.dtos.frontend.response;

public record BffErrorDtoResponse(
        Integer codError,
        String msgError
) {
}