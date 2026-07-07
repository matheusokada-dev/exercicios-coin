package br.com.coin.bffcadastroprodutos.dtos.bff;

public record BffErrorDTO(
        Integer codError,
        String msgError
) {
}