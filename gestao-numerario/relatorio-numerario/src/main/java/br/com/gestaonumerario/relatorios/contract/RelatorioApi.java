package br.com.gestaonumerario.relatorios.contract;

import br.com.gestaonumerario.relatorios.dto.GerarRelatorioRequest;
import br.com.gestaonumerario.relatorios.dto.GerarRelatorioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;

@Tag(
        name = "Relatórios",
        description = "Geração interna de arquivos de relatório em formato XLSX."
)
public interface RelatorioApi {

    @Operation(
            operationId = "gerarRelatorio",
            summary = "Gerar relatório em Excel",
            description = """
                    Recebe colunas, linhas e metadados e devolve um arquivo XLSX
                    codificado em Base64. Aceita até 25 colunas e 25.000 linhas.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Relatório gerado com sucesso.",
                    content = @Content(schema = @Schema(implementation = GerarRelatorioResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido ou limite de geração excedido.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falha interna durante a geração do relatório.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    GerarRelatorioResponse gerar(GerarRelatorioRequest request);
}
