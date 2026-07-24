package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.mapper.AgenciaRestMapper;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.AgenciaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AtualizarAgenciaRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.CriarAgenciaRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.DetalheAgenciaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.PaginaResponse;
import br.com.gestaonumerario.api.core.domain.enums.CampoOrdenacaoAgencia;
import br.com.gestaonumerario.api.core.domain.enums.DirecaoOrdenacao;
import br.com.gestaonumerario.api.core.domain.model.FiltroAgencia;
import br.com.gestaonumerario.api.port.input.AgenciaInputPort;
import br.com.gestaonumerario.api.port.input.AgenciaInputPort;
import br.com.gestaonumerario.api.port.input.AgenciaInputPort;
import br.com.gestaonumerario.api.port.input.AgenciaInputPort;
import br.com.gestaonumerario.api.port.input.AgenciaInputPort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/agencias")
@RequiredArgsConstructor
@Validated
public class AgenciaController {

    private final AgenciaInputPort criarUseCase;
    private final AgenciaInputPort atualizarUseCase;
    private final AgenciaInputPort consultarUseCase;
    private final AgenciaInputPort desativarUseCase;
    private final AgenciaInputPort detalharUseCase;
    private final AgenciaRestMapper mapper;

    @PostMapping
    public ResponseEntity<AgenciaResponse> criar(@Valid @RequestBody CriarAgenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(criarUseCase.criar(mapper.toCommand(request))));
    }

    @GetMapping
    public PaginaResponse<AgenciaResponse> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Boolean alerta,
            @RequestParam(defaultValue = "CODIGO") CampoOrdenacaoAgencia ordenarPor,
            @RequestParam(defaultValue = "ASC") DirecaoOrdenacao direcao,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho
    ) {
        var resultado = consultarUseCase.listar(
                new FiltroAgencia(busca, ativo, alerta, ordenarPor, direcao, pagina, tamanho));

        return new PaginaResponse<>(resultado.itens().stream().map(mapper::toResponse).toList(),
                resultado.pagina(), resultado.tamanho(), resultado.totalItens(), resultado.totalPaginas());
    }

    @GetMapping("/{agenciaId}")
    public AgenciaResponse buscarPorId(@PathVariable Long agenciaId) {
        return mapper.toResponse(consultarUseCase.buscarPorId(agenciaId));
    }

    @GetMapping("/{agenciaId}/detalhe")
    public DetalheAgenciaResponse detalhar(@PathVariable Long agenciaId) {
        return mapper.toResponse(detalharUseCase.detalhar(agenciaId));
    }

    @PutMapping("/{agenciaId}")
    public AgenciaResponse atualizar(
            @PathVariable Long agenciaId, @Valid @RequestBody AtualizarAgenciaRequest request
    ) {
        return mapper.toResponse(atualizarUseCase.atualizar(mapper.toCommand(agenciaId, request)));
    }

    @DeleteMapping("/{agenciaId}")
    public ResponseEntity<Void> desativar(@PathVariable Long agenciaId) {
        desativarUseCase.desativar(agenciaId);
        return ResponseEntity.noContent().build();
    }
}



