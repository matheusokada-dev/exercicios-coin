package br.com.gestaonumerario.bff.dto;

import java.util.List;

public record PaginaResponse<T>(List<T> itens, int pagina, int tamanho, long totalItens, int totalPaginas) {
}
