package br.com.gestaonumerario.bff.dto;

public record MetadadosRelatorio(
        String titulo,
        String subtitulo,
        String periodo,
        String usuario,
        String nomeArquivo
) {
}
