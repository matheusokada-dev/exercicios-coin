package br.com.gestaonumerario.bff.dto;

import java.util.List;

public record GerarRelatorioRequest(
        List<String> colunas,
        List<List<Object>> linhas,
        MetadadosRelatorio metadados,
        String formato
) {
}
