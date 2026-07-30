package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.contract.SolicitacaoApi;
import br.com.gestaonumerario.bff.dto.AprovarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.AtenderSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.dto.RejeitarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.SolicitacaoResponse;
import br.com.gestaonumerario.bff.dto.SolicitarAbastecimentoRequest;
import br.com.gestaonumerario.bff.service.SolicitacaoService;
import java.time.LocalDate;
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

@Validated
@RestController
@RequestMapping("/api/v1/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoController implements SolicitacaoApi {

    private final SolicitacaoService solicitacaoService;

    @GetMapping
    @Override
    public PaginaResponse<SolicitacaoResponse> listar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(required = false) Long agenciaId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return solicitacaoService.listar(
                authorization,
                agenciaId,
                status,
                dataInicio,
                dataFim,
                pagina,
                tamanho
        );
    }

    @PostMapping
    @Override
    public ResponseEntity<SolicitacaoResponse> criar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody SolicitarAbastecimentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        solicitacaoService.criar(
                                authorization,
                                request
                        )
                );
    }

    @PutMapping("/{id}/aprovar")
    @Override
    public SolicitacaoResponse aprovar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable Long id,
            @RequestBody AprovarSolicitacaoRequest request) {
        return solicitacaoService.aprovar(
                authorization,
                id,
                request
        );
    }

    @PutMapping("/{id}/rejeitar")
    @Override
    public SolicitacaoResponse rejeitar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable Long id,
            @RequestBody RejeitarSolicitacaoRequest request) {
        return solicitacaoService.rejeitar(
                authorization,
                id,
                request
        );
    }

    @PutMapping("/{id}/atender")
    @Override
    public SolicitacaoResponse atender(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable Long id,
            @RequestBody AtenderSolicitacaoRequest request) {
        return solicitacaoService.atender(
                authorization,
                id,
                request
        );
    }
}
