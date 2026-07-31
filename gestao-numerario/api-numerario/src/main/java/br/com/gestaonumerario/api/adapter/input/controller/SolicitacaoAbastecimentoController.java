package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.adapter.input.contract.SolicitacaoAbastecimentoApi;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AprovarSolicitacaoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AtenderSolicitacaoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.RejeitarSolicitacaoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.SolicitarAbastecimentoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.PaginaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.SolicitacaoAbastecimentoResponse;
import br.com.gestaonumerario.api.adapter.input.mapper.SolicitacaoAbastecimentoRestMapper;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacao;
import br.com.gestaonumerario.api.core.domain.model.FiltroSolicitacao;
import br.com.gestaonumerario.api.port.input.SolicitacaoInputPort;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/solicitacoes")
@RequiredArgsConstructor
@Validated
public class SolicitacaoAbastecimentoController implements SolicitacaoAbastecimentoApi {

    private final SolicitacaoInputPort solicitacaoUseCase;
    private final SolicitacaoAbastecimentoRestMapper mapper;

    @GetMapping
    @Override
    public PaginaResponse<SolicitacaoAbastecimentoResponse> consultar(
            @RequestParam(required = false) Long agenciaId,
            @RequestParam(required = false) StatusSolicitacao status,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        var resultado = solicitacaoUseCase.consultar(
                new FiltroSolicitacao(
                        agenciaId,
                        status,
                        dataInicio,
                        dataFim,
                        pagina,
                        tamanho
                )
        );
        return new PaginaResponse<>(
                resultado.itens()
                        .stream()
                        .map(mapper::toResponse)
                        .toList(),
                resultado.pagina(),
                resultado.tamanho(),
                resultado.totalItens(),
                resultado.totalPaginas()
        );
    }

    @PostMapping
    @Override
    public ResponseEntity<SolicitacaoAbastecimentoResponse> solicitar(
            @RequestBody SolicitarAbastecimentoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        SolicitacaoAbastecimentoResponse response = mapper.toResponse(
                solicitacaoUseCase.solicitar(
                        mapper.toCommand(
                                request,
                                usuario
                        )
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{solicitacaoId}/aprovar")
    @Override
    public SolicitacaoAbastecimentoResponse aprovar(
            @PathVariable Long solicitacaoId,
            @RequestBody AprovarSolicitacaoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return mapper.toResponse(
                solicitacaoUseCase.aprovar(
                        mapper.toCommand(
                                solicitacaoId,
                                request,
                                usuario
                        )
                )
        );
    }

    @PutMapping("/{solicitacaoId}/rejeitar")
    @Override
    public SolicitacaoAbastecimentoResponse rejeitar(
            @PathVariable Long solicitacaoId,
            @RequestBody RejeitarSolicitacaoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return mapper.toResponse(
                solicitacaoUseCase.rejeitar(
                        mapper.toCommand(
                                solicitacaoId,
                                request,
                                usuario
                        )
                )
        );
    }

    @PutMapping("/{solicitacaoId}/atender")
    @Override
    public SolicitacaoAbastecimentoResponse atender(
            @PathVariable Long solicitacaoId,
            @RequestBody AtenderSolicitacaoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return mapper.toResponse(
                solicitacaoUseCase.atender(
                        mapper.toCommand(
                                solicitacaoId,
                                request,
                                usuario
                        )
                )
        );
    }
}
