package br.com.coin.bffcadastroprodutos.dtos.frontend;

public record BffErrorDtoResponse(
        Integer codError,
        String msgError
) {
}