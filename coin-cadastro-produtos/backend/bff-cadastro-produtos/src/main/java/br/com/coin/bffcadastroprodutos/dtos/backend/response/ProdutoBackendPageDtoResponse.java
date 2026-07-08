package br.com.coin.bffcadastroprodutos.dtos.backend.response;

import java.util.List;

public record ProdutoBackendPageDtoResponse<T>(
        List<T> content,
        Long totalElements,
        Integer totalPages,
        Integer size,
        Integer number
) {
}