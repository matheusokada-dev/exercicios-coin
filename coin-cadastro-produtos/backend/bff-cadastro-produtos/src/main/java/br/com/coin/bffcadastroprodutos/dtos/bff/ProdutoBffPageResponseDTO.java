package br.com.coin.bffcadastroprodutos.dtos.bff;

import java.util.List;

public record ProdutoBffPageResponseDTO<T>(
        List<T> content,
        Long totalElements,
        Integer totalPages,
        Integer size,
        Integer number
) {
}