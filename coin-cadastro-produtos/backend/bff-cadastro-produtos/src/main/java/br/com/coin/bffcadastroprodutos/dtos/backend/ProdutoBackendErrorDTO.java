package br.com.coin.bffcadastroprodutos.dtos.backend;

public record ProdutoBackendErrorDTO(
        Integer codError,
        String msgError
) {
}