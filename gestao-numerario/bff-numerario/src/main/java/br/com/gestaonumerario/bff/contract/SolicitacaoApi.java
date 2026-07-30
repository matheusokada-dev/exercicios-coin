package br.com.gestaonumerario.bff.contract;

import br.com.gestaonumerario.bff.dto.AprovarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.AtenderSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.dto.RejeitarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.SolicitacaoResponse;
import br.com.gestaonumerario.bff.dto.SolicitarAbastecimentoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;

@Tag(name = "Solicitações de abastecimento", description = "Contrato legado de abastecimento consumido pelo frontend.")
public interface SolicitacaoApi {

    @Operation(summary = "Listar solicitações de abastecimento", description = "Consulta solicitações paginadas por agência, situação e período.")
    PaginaResponse<SolicitacaoResponse> listar(@Parameter(hidden = true) String authorization, Long agenciaId,
            String status, LocalDate dataInicio, LocalDate dataFim,
            @Min(0) int pagina, @Min(1) @Max(100) int tamanho);

    @Operation(summary = "Criar solicitação de abastecimento", description = "Registra uma nova solicitação pendente para a agência.")
    ResponseEntity<SolicitacaoResponse> criar(
            @Parameter(hidden = true) String authorization,
            @Valid SolicitarAbastecimentoRequest request);

    @Operation(summary = "Aprovar solicitação de abastecimento", description = "Aprova uma solicitação pendente e registra o usuário responsável.")
    SolicitacaoResponse aprovar(
            @Parameter(hidden = true) String authorization,
            Long id,
            @Valid AprovarSolicitacaoRequest request);

    @Operation(summary = "Rejeitar solicitação de abastecimento", description = "Rejeita uma solicitação pendente com justificativa obrigatória.")
    SolicitacaoResponse rejeitar(
            @Parameter(hidden = true) String authorization,
            Long id,
            @Valid RejeitarSolicitacaoRequest request);

    @Operation(summary = "Atender solicitação de abastecimento", description = "Efetiva o abastecimento aprovado e atualiza o saldo da agência.")
    SolicitacaoResponse atender(
            @Parameter(hidden = true) String authorization,
            Long id,
            @Valid AtenderSolicitacaoRequest request);
}
