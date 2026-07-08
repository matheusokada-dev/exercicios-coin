package br.com.coin.bffcadastroprodutos.dtos.frontend;

import java.util.List;

public record ProdutoBffPageDtoResponse<T>(
        List<T> content,
        Long totalElements,
        Integer totalPages,
        Integer size,
        Integer number
) {
}