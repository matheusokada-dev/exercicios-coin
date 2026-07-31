package br.com.gestaonumerario.api.adapter.input.controller.dto.response;

import java.util.List;

public record PaginaResponse<T>(List<T> itens, int pagina, int tamanho, long totalItens, int totalPaginas) {
}
