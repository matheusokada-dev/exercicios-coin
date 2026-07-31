package br.com.gestaonumerario.relatorios.dto;

import java.time.LocalDateTime;

public record GerarRelatorioResponse(
        String conteudo,
        String nomeArquivo,
        String formato,
        LocalDateTime dataGeracao
) {
}
