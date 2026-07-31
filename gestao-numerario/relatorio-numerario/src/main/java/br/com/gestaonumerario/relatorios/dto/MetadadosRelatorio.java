package br.com.gestaonumerario.relatorios.dto;

import jakarta.validation.constraints.NotBlank;

public record MetadadosRelatorio(
        @NotBlank String titulo,
        String subtitulo,
        String periodo,
        String usuario,
        @NotBlank String nomeArquivo
) {
}
