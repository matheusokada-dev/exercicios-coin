package br.com.gestaonumerario.bff.contract;

import br.com.gestaonumerario.bff.dto.AjustarDivergenciaRequest;
import br.com.gestaonumerario.bff.dto.CargaInicialTesourariaRequest;
import br.com.gestaonumerario.bff.dto.ConciliarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.CriarSolicitacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.DecidirSolicitacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.DetalheSolicitacaoNumerarioResponse;
import br.com.gestaonumerario.bff.dto.ExecutarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.HistoricoSolicitacaoResponse;
import br.com.gestaonumerario.bff.dto.OcorrenciaOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.OperacaoNumerarioResponse;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.dto.ProgramarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.ReceberOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.SolicitacaoNumerarioResponse;
import br.com.gestaonumerario.bff.dto.UnidadeOperacionalResponse;
import br.com.gestaonumerario.bff.dto.VersaoOperacaoNumerarioRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "Tesouraria",
        description = "Consultas e comandos do fluxo evoluído de solicitações e operações de numerário."
)
public interface ConsultaNumerarioApi {

    @Operation(
            summary = "Consultar solicitações de numerário",
            description = "Retorna solicitações paginadas com filtros opcionais de agência, tipo, situação, rota e período."
    )
    PaginaResponse<SolicitacaoNumerarioResponse> solicitacoes(
            @Parameter(hidden = true) String auth,
            Long agenciaId,
            String tipo,
            String status,
            Long origemId,
            Long destinoId,
            LocalDate dataInicio,
            LocalDate dataFim,
            @Min(0) int pagina,
            @Min(1) @Max(100) int tamanho);

    @Operation(
            summary = "Detalhar solicitação de numerário",
            description = "Retorna dados, situação atual, operação vinculada e histórico resumido."
    )
    DetalheSolicitacaoNumerarioResponse detalhe(@Parameter(hidden = true) String auth, Long id);

    @Operation(
            summary = "Consultar histórico da solicitação",
            description = "Lista cronologicamente as alterações imutáveis registradas para auditoria."
    )
    List<HistoricoSolicitacaoResponse> historico(@Parameter(hidden = true) String auth, Long id);

    @Operation(
            summary = "Consultar unidades operacionais",
            description = "Lista Tesouraria e agências, com filtro opcional por tipo."
    )
    List<UnidadeOperacionalResponse> unidades(@Parameter(hidden = true) String auth, String tipo);

    @Operation(
            summary = "Consultar operações de numerário",
            description = "Retorna operações logísticas paginadas por situação, origem, destino e período."
    )
    PaginaResponse<OperacaoNumerarioResponse> operacoes(
            @Parameter(hidden = true) String auth,
            String status,
            Long origemId,
            Long destinoId,
            LocalDate dataInicio,
            LocalDate dataFim,
            @Min(0) int pagina,
            @Min(1) @Max(100) int tamanho);

    @Operation(
            summary = "Criar solicitação de numerário",
            description = "Cria uma solicitação de suprimento ou recolhimento em estado pendente."
    )
    ResponseEntity<SolicitacaoNumerarioResponse> criar(
            @Parameter(hidden = true) String auth,
            @Valid CriarSolicitacaoNumerarioRequest request);

    @Operation(
            summary = "Aprovar solicitação de numerário",
            description = "Aprova uma solicitação pendente usando controle otimista de versão."
    )
    SolicitacaoNumerarioResponse aprovar(
            @Parameter(hidden = true) String auth,
            Long id,
            @Valid DecidirSolicitacaoNumerarioRequest request);

    @Operation(
            summary = "Rejeitar solicitação de numerário",
            description = "Rejeita uma solicitação pendente e registra a justificativa."
    )
    SolicitacaoNumerarioResponse rejeitar(
            @Parameter(hidden = true) String auth,
            Long id,
            @Valid DecidirSolicitacaoNumerarioRequest request);

    @Operation(
            summary = "Cancelar solicitação de numerário",
            description = "Cancela a solicitação quando o estado atual ainda permite a transição."
    )
    SolicitacaoNumerarioResponse cancelar(
            @Parameter(hidden = true) String auth,
            Long id,
            @Valid DecidirSolicitacaoNumerarioRequest request);

    @Operation(
            summary = "Programar operação de numerário",
            description = "Completa a rota da solicitação aprovada e cria a operação logística de forma idempotente."
    )
    OperacaoNumerarioResponse programar(
            @Parameter(hidden = true) String auth,
            @Parameter(name = "Idempotency-Key", required = true, description = "Chave única do comando.") String idempotencyKey,
            Long id,
            @Valid ProgramarOperacaoNumerarioRequest request);

    @Operation(
            summary = "Iniciar separação",
            description = "Move a operação programada para separação, validando a versão atual."
    )
    OperacaoNumerarioResponse separar(
            @Parameter(hidden = true) String auth,
            Long id,
            @Valid VersaoOperacaoNumerarioRequest request);

    @Operation(
            summary = "Expedir numerário",
            description = "Debita a origem e registra o valor como numerário em trânsito de forma idempotente."
    )
    OperacaoNumerarioResponse expedir(
            @Parameter(hidden = true) String auth,
            @Parameter(name = "Idempotency-Key", required = true, description = "Chave única do comando.") String idempotencyKey,
            Long id,
            @Valid ExecutarOperacaoNumerarioRequest request);

    @Operation(
            summary = "Registrar ocorrência logística",
            description = "Registra uma ocorrência auditável durante a separação ou o transporte."
    )
    OperacaoNumerarioResponse ocorrencia(
            @Parameter(hidden = true) String auth,
            Long id,
            @Valid OcorrenciaOperacaoNumerarioRequest request);

    @Operation(
            summary = "Confirmar recebimento",
            description = "Credita o destino e abre divergência quando o valor recebido difere do expedido."
    )
    OperacaoNumerarioResponse receber(
            @Parameter(hidden = true) String auth,
            @Parameter(name = "Idempotency-Key", required = true, description = "Chave única do comando.") String idempotencyKey,
            Long id,
            @Valid ReceberOperacaoNumerarioRequest request);

    @Operation(
            summary = "Conciliar divergência",
            description = "Conclui a análise de uma operação recebida com divergência."
    )
    OperacaoNumerarioResponse conciliar(
            @Parameter(hidden = true) String auth,
            @Parameter(name = "Idempotency-Key", required = true, description = "Chave única do comando.") String idempotencyKey,
            Long id,
            @Valid ConciliarOperacaoNumerarioRequest request);

    @Operation(
            summary = "Realizar carga inicial da Tesouraria",
            description = "Inicializa uma única vez o saldo da Tesouraria ativa, com idempotência e auditoria."
    )
    UnidadeOperacionalResponse carga(
            @Parameter(hidden = true) String auth,
            @Parameter(name = "Idempotency-Key", required = true, description = "Chave única do comando.") String idempotencyKey,
            @Valid CargaInicialTesourariaRequest request);

    @Operation(
            summary = "Ajustar divergência financeira",
            description = "Registra o ajuste autorizado sem exceder a divergência apurada na operação."
    )
    UnidadeOperacionalResponse ajustar(
            @Parameter(hidden = true) String auth,
            @Parameter(name = "Idempotency-Key", required = true, description = "Chave única do comando.") String idempotencyKey,
            Long id,
            @Valid AjustarDivergenciaRequest request);
}
