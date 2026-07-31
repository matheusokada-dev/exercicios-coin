package br.com.gestaonumerario.bff.contract;

import br.com.gestaonumerario.bff.dto.GerarLivroCaixaRequest;
import br.com.gestaonumerario.bff.dto.GerarRelatorioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Relatórios", description = "Geração centralizada de arquivos para download no frontend.")
public interface RelatorioApi {

    @Operation(summary = "Gerar Livro Caixa em Excel", description = "Consulta todas as movimentações do período e devolve o arquivo XLSX codificado em Base64.")
    GerarRelatorioResponse gerarLivroCaixa(@Parameter(hidden = true) String authorization, GerarLivroCaixaRequest request);
}
