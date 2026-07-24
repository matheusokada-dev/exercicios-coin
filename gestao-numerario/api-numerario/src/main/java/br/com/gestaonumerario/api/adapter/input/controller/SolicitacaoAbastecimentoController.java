package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.mapper.SolicitacaoAbastecimentoRestMapper;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AprovarSolicitacaoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AtenderSolicitacaoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.RejeitarSolicitacaoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.SolicitacaoAbastecimentoResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.SolicitarAbastecimentoRequest;
import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.PaginaResponse;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacao;
import br.com.gestaonumerario.api.core.domain.model.FiltroSolicitacao;
import br.com.gestaonumerario.api.port.input.SolicitacaoInputPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.validation.annotation.Validated;
import java.time.LocalDate;
import br.com.gestaonumerario.api.port.input.SolicitacaoInputPort;
import br.com.gestaonumerario.api.port.input.SolicitacaoInputPort;
import br.com.gestaonumerario.api.port.input.SolicitacaoInputPort;
import br.com.gestaonumerario.api.port.input.SolicitacaoInputPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/solicitacoes")
@RequiredArgsConstructor
@Validated
public class SolicitacaoAbastecimentoController {

    private final SolicitacaoInputPort solicitarUseCase;
    private final SolicitacaoInputPort aprovarUseCase;
    private final SolicitacaoInputPort rejeitarUseCase;
    private final SolicitacaoInputPort atenderUseCase;
    private final SolicitacaoInputPort consultarUseCase;
    private final SolicitacaoAbastecimentoRestMapper mapper;

    @GetMapping
    public PaginaResponse<SolicitacaoAbastecimentoResponse> consultar(
            @RequestParam(required = false) Long agenciaId, @RequestParam(required = false) StatusSolicitacao status,
            @RequestParam(required = false) LocalDate dataInicio, @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
        var resultado = consultarUseCase.consultar(new FiltroSolicitacao(agenciaId, status, dataInicio, dataFim, pagina, tamanho));
        return new PaginaResponse<>(resultado.itens().stream().map(mapper::toResponse).toList(), resultado.pagina(),
                resultado.tamanho(), resultado.totalItens(), resultado.totalPaginas());
    }

    @PostMapping
    public ResponseEntity<SolicitacaoAbastecimentoResponse> solicitar(
            @Valid @RequestBody SolicitarAbastecimentoRequest request, @AuthenticationPrincipal UsuarioAutenticado usuario
    ) {
        SolicitacaoAbastecimentoResponse response = mapper.toResponse(
                solicitarUseCase.solicitar(mapper.toCommand(request, usuario)));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{solicitacaoId}/aprovar")
    public SolicitacaoAbastecimentoResponse aprovar(
            @PathVariable Long solicitacaoId,
            @Valid @RequestBody AprovarSolicitacaoRequest request, @AuthenticationPrincipal UsuarioAutenticado usuario
    ) {
        return mapper.toResponse(aprovarUseCase.aprovar(mapper.toCommand(solicitacaoId, request, usuario)));
    }

    @PutMapping("/{solicitacaoId}/rejeitar")
    public SolicitacaoAbastecimentoResponse rejeitar(
            @PathVariable Long solicitacaoId,
            @Valid @RequestBody RejeitarSolicitacaoRequest request, @AuthenticationPrincipal UsuarioAutenticado usuario
    ) {
        return mapper.toResponse(rejeitarUseCase.rejeitar(mapper.toCommand(solicitacaoId, request, usuario)));
    }

    @PutMapping("/{solicitacaoId}/atender")
    public SolicitacaoAbastecimentoResponse atender(
            @PathVariable Long solicitacaoId,
            @Valid @RequestBody AtenderSolicitacaoRequest request, @AuthenticationPrincipal UsuarioAutenticado usuario
    ) {
        return mapper.toResponse(atenderUseCase.atender(mapper.toCommand(solicitacaoId, request, usuario)));
    }
}



