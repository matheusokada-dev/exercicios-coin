package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.dto.MovimentacaoResponse;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.dto.RegistrarMovimentacaoRequest;
import br.com.gestaonumerario.bff.service.MovimentacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/api/v1/movimentacoes")
@RequiredArgsConstructor
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @GetMapping
    public PaginaResponse<MovimentacaoResponse> listar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(required = false) Long agenciaId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho
    ) {
        return movimentacaoService.listar(
                authorization, agenciaId, tipo, dataInicio, dataFim, pagina, tamanho);
    }

    @PostMapping
    public ResponseEntity<MovimentacaoResponse> criar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody RegistrarMovimentacaoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movimentacaoService.criar(authorization, request));
    }
}
