package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.contract.AgenciaApi;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AtualizarAgenciaRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.CriarAgenciaRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.AgenciaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.DetalheAgenciaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.PaginaResponse;
import br.com.gestaonumerario.api.adapter.input.mapper.AgenciaRestMapper;
import br.com.gestaonumerario.api.core.domain.enums.CampoOrdenacaoAgencia;
import br.com.gestaonumerario.api.core.domain.enums.DirecaoOrdenacao;
import br.com.gestaonumerario.api.core.domain.model.FiltroAgencia;
import br.com.gestaonumerario.api.port.input.AgenciaInputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agencias")
@RequiredArgsConstructor
@Validated
public class AgenciaController implements AgenciaApi {

    private final AgenciaInputPort agenciaUseCase;
    private final AgenciaRestMapper mapper;

    @PostMapping
    @Override
    public ResponseEntity<AgenciaResponse> criar(@RequestBody CriarAgenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(agenciaUseCase.criar(mapper.toCommand(request))));
    }

    @GetMapping
    @Override
    public PaginaResponse<AgenciaResponse> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Boolean alerta,
            @RequestParam(defaultValue = "CODIGO") CampoOrdenacaoAgencia ordenarPor,
            @RequestParam(defaultValue = "ASC") DirecaoOrdenacao direcao,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        var resultado = agenciaUseCase.listar(
                new FiltroAgencia(
                        busca,
                        ativo,
                        alerta,
                        ordenarPor,
                        direcao,
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

    @GetMapping("/{agenciaId}")
    @Override
    public AgenciaResponse buscarPorId(@PathVariable Long agenciaId) {
        return mapper.toResponse(agenciaUseCase.buscarPorId(agenciaId));
    }

    @GetMapping("/{agenciaId}/detalhe")
    @Override
    public DetalheAgenciaResponse detalhar(@PathVariable Long agenciaId) {
        return mapper.toResponse(agenciaUseCase.detalhar(agenciaId));
    }

    @PutMapping("/{agenciaId}")
    @Override
    public AgenciaResponse atualizar(
            @PathVariable Long agenciaId,
            @RequestBody AtualizarAgenciaRequest request) {
        return mapper.toResponse(
                agenciaUseCase.atualizar(
                        mapper.toCommand(
                                agenciaId,
                                request
                        )
                )
        );
    }

    @DeleteMapping("/{agenciaId}")
    @Override
    public ResponseEntity<Void> desativar(@PathVariable Long agenciaId) {
        agenciaUseCase.desativar(agenciaId);
        return ResponseEntity.noContent()
                .build();
    }
}
