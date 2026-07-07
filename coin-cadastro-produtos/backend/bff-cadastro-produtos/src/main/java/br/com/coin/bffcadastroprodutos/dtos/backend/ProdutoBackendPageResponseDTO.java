package br.com.coin.bffcadastroprodutos.dtos.backend;

import java.util.List;

public record ProdutoBackendPageResponseDTO<T>(
        List<T> content,
        Long totalElements,
        Integer totalPages,
        Integer size,
        Integer number
) {
}