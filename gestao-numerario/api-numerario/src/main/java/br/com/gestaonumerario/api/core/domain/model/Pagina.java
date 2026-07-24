package br.com.gestaonumerario.api.core.domain.model;

import java.util.List;

public record Pagina<T>(
        List<T> itens,
        int pagina,
        int tamanho,
        long totalItens,
        int totalPaginas
) {
}

