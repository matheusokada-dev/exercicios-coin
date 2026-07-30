package br.com.gestaonumerario.bff.dto;

public record GerarRelatorioResponse(
        String conteudo,
        String nomeArquivo,
        String formato,
        String dataGeracao
) {
}
