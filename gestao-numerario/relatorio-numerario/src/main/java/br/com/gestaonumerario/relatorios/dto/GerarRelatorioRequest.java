package br.com.gestaonumerario.relatorios.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GerarRelatorioRequest(
        @NotEmpty List<String> colunas,
        @NotNull List<List<JsonNode>> linhas,
        @NotNull @Valid MetadadosRelatorio metadados,
        @NotNull String formato
) {
}
