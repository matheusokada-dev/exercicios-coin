package br.com.coin.bffcadastroprodutos.dtos.backend;

public record ProdutoBackendErrorDtoResponse(
        Integer codError,
        String msgError
) {
}