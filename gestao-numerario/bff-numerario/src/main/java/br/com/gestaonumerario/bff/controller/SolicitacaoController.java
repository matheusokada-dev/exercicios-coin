package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.dto.AprovarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.AtenderSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.dto.RejeitarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.SolicitacaoResponse;
import br.com.gestaonumerario.bff.dto.SolicitarAbastecimentoRequest;
import br.com.gestaonumerario.bff.service.SolicitacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/api/v1/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    @GetMapping
    public PaginaResponse<SolicitacaoResponse> listar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(required = false) Long agenciaId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho
    ) {
        return solicitacaoService.listar(
                authorization, agenciaId, status, dataInicio, dataFim, pagina, tamanho);
    }

    @PostMapping
    public ResponseEntity<SolicitacaoResponse> criar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody SolicitarAbastecimentoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoService.criar(authorization, request));
    }

    @PutMapping("/{id}/aprovar")
    public SolicitacaoResponse aprovar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody AprovarSolicitacaoRequest request
    ) {
        return solicitacaoService.aprovar(authorization, id, request);
    }

    @PutMapping("/{id}/rejeitar")
    public SolicitacaoResponse rejeitar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody RejeitarSolicitacaoRequest request
    ) {
        return solicitacaoService.rejeitar(authorization, id, request);
    }

    @PutMapping("/{id}/atender")
    public SolicitacaoResponse atender(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody AtenderSolicitacaoRequest request
    ) {
        return solicitacaoService.atender(authorization, id, request);
    }
}
