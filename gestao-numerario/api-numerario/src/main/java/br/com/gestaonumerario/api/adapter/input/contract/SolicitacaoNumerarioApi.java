package br.com.gestaonumerario.api.adapter.input.contract;

import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.CriarSolicitacaoNumerarioRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.DecidirSolicitacaoNumerarioRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.DetalheSolicitacaoNumerarioResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.PaginaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.SolicitacaoNumerarioResponse;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoOperacaoNumerario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Solicitações de numerário",
        description = "Criação, consulta, decisão, cancelamento e histórico de solicitações.")
public interface SolicitacaoNumerarioApi {

    @Operation(
            summary = "Criar solicitação de numerário",
            description = "Cria uma solicitação de SUPRIMENTO ou RECOLHIMENTO em estado PENDENTE.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    schema = @Schema(implementation = CriarSolicitacaoNumerarioRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "tipoOperacao": "SUPRIMENTO",
                              "agenciaId": 12,
                              "valor": 1050.00,
                              "motivo": "Recomposição do saldo operacional",
                              "dataDesejada": "2026-07-28"
                            }
                            """)))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitação criada"),
            @ApiResponse(responseCode = "400", description = "Campo ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Agência ou usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Já existe solicitação aberta"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada")
    })
    ResponseEntity<SolicitacaoNumerarioResponse> criar(
            @Valid CriarSolicitacaoNumerarioRequest request,
            @Parameter(hidden = true) UsuarioAutenticado usuario);

    @Operation(
            summary = "Consultar solicitações",
            description = "Retorna solicitações paginadas. Todos os filtros são opcionais.")
    PaginaResponse<SolicitacaoNumerarioResponse> consultar(
            Long agenciaId,
            TipoOperacaoNumerario tipo,
            StatusSolicitacaoNumerario status,
            Long origemId,
            Long destinoId,
            LocalDate dataInicio,
            LocalDate dataFim,
            @Min(0) int pagina,
            @Min(1) @Max(100) int tamanho);

    @Operation(summary = "Detalhar solicitação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalhe encontrado"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    DetalheSolicitacaoNumerarioResponse detalhar(Long id);

    @Operation(summary = "Consultar histórico imutável da solicitação")
    List<DetalheSolicitacaoNumerarioResponse.HistoricoResumo> historico(Long id);

    @Operation(summary = "Aprovar solicitação",
            description = "Somente uma solicitação PENDENTE pode ser aprovada.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = """
                    {"justificativa":"Operação aprovada pelo gestor","versao":0}
                    """)))
    SolicitacaoNumerarioResponse aprovar(
            Long id,
            @Valid DecidirSolicitacaoNumerarioRequest request,
            @Parameter(hidden = true) UsuarioAutenticado usuario);

    @Operation(summary = "Rejeitar solicitação",
            description = "Somente uma solicitação PENDENTE pode ser rejeitada.")
    SolicitacaoNumerarioResponse rejeitar(
            Long id,
            @Valid DecidirSolicitacaoNumerarioRequest request,
            @Parameter(hidden = true) UsuarioAutenticado usuario);

    @Operation(summary = "Cancelar solicitação",
            description = "O cancelamento respeita o estado atual e exige justificativa.")
    SolicitacaoNumerarioResponse cancelar(
            Long id,
            @Valid DecidirSolicitacaoNumerarioRequest request,
            @Parameter(hidden = true) UsuarioAutenticado usuario);
}
