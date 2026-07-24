package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.mapper.MovimentacaoRestMapper;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.PaginaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.MovimentacaoResponse;
import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.FiltroMovimentacao;
import br.com.gestaonumerario.api.port.input.MovimentacaoInputPort;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.RegistrarMovimentacaoRequest;
import br.com.gestaonumerario.api.port.input.MovimentacaoInputPort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;

import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/movimentacoes")
@RequiredArgsConstructor
@Validated
public class MovimentacaoController {

    private final MovimentacaoInputPort registrarUseCase;
    private final MovimentacaoInputPort consultarUseCase;
    private final MovimentacaoRestMapper mapper;

    @PostMapping
    public ResponseEntity<MovimentacaoResponse> registrar(
            @Valid @RequestBody RegistrarMovimentacaoRequest request, @AuthenticationPrincipal UsuarioAutenticado usuario
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(registrarUseCase.registrar(mapper.toCommand(request, usuario))));
    }

    @GetMapping
    public PaginaResponse<MovimentacaoResponse> consultar(
            @RequestParam(required = false) Long agenciaId,
            @RequestParam(required = false) TipoMovimentacao tipo,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho
    ) {
        var resultado = consultarUseCase.consultar(
                new FiltroMovimentacao(agenciaId, tipo, dataInicio, dataFim, pagina, tamanho));

        return new PaginaResponse<>(resultado.itens().stream().map(mapper::toResponse).toList(),
                resultado.pagina(), resultado.tamanho(), resultado.totalItens(), resultado.totalPaginas());
    }
}



