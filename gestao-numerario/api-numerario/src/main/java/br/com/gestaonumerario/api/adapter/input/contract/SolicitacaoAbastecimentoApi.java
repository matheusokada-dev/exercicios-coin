package br.com.gestaonumerario.api.adapter.input.contract;

import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AprovarSolicitacaoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AtenderSolicitacaoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.RejeitarSolicitacaoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.SolicitarAbastecimentoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.PaginaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.SolicitacaoAbastecimentoResponse;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "Solicitações de abastecimento",
        description = "Fluxo legado de solicitação, aprovação, rejeição e atendimento."
)
public interface SolicitacaoAbastecimentoApi {

    @Operation(
            summary = "Consultar solicitações de abastecimento",
            description = "Retorna solicitações paginadas por agência, situação e período."
    )
    PaginaResponse<SolicitacaoAbastecimentoResponse> consultar(
            Long agenciaId,
            StatusSolicitacao status,
            LocalDate dataInicio,
            LocalDate dataFim,
            @Min(0) int pagina,
            @Min(1) @Max(100) int tamanho);

    @Operation(
            summary = "Criar solicitação de abastecimento",
            description = "Registra uma solicitação pendente para a agência informada."
    )
    ResponseEntity<SolicitacaoAbastecimentoResponse> solicitar(
            @Valid SolicitarAbastecimentoRequest request,
            @Parameter(hidden = true) UsuarioAutenticado usuario);

    @Operation(
            summary = "Aprovar solicitação de abastecimento",
            description = "Aprova uma solicitação pendente usando a versão informada."
    )
    SolicitacaoAbastecimentoResponse aprovar(
            Long solicitacaoId,
            @Valid AprovarSolicitacaoRequest request,
            @Parameter(hidden = true) UsuarioAutenticado usuario);

    @Operation(
            summary = "Rejeitar solicitação de abastecimento",
            description = "Rejeita uma solicitação pendente e registra a justificativa."
    )
    SolicitacaoAbastecimentoResponse rejeitar(
            Long solicitacaoId,
            @Valid RejeitarSolicitacaoRequest request,
            @Parameter(hidden = true) UsuarioAutenticado usuario);

    @Operation(
            summary = "Atender solicitação de abastecimento",
            description = "Efetiva o abastecimento aprovado e registra a movimentação financeira."
    )
    SolicitacaoAbastecimentoResponse atender(
            Long solicitacaoId,
            @Valid AtenderSolicitacaoRequest request,
            @Parameter(hidden = true) UsuarioAutenticado usuario);
}
