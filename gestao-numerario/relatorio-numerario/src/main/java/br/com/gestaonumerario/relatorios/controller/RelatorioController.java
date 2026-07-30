package br.com.gestaonumerario.relatorios.controller;

import br.com.gestaonumerario.relatorios.dto.GerarRelatorioRequest;
import br.com.gestaonumerario.relatorios.dto.GerarRelatorioResponse;
import br.com.gestaonumerario.relatorios.service.GerarRelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/relatorios")
@Tag(
        name = "Relatórios",
        description = "Geração interna de arquivos de relatório em formato XLSX."
)
public class RelatorioController {

    private final GerarRelatorioService service;

    public RelatorioController(GerarRelatorioService service) {
        this.service = service;
    }

    @PostMapping("/gerar")
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
    public GerarRelatorioResponse gerar(@Valid @RequestBody GerarRelatorioRequest request) {
        return service.gerar(request);
    }
}
