package br.com.coin.bffcadastroprodutos.dtos.backend.response;

public record ProdutoBackendErrorDtoResponse(
        Integer codError,
        String msgError
) {
}