package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.contract.AgenciaApi;
import br.com.gestaonumerario.bff.dto.AgenciaResponse;
import br.com.gestaonumerario.bff.dto.AtualizarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.CriarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.DetalheAgenciaResponse;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.service.AgenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/agencias")
@RequiredArgsConstructor
public class AgenciaController implements AgenciaApi {

    private final AgenciaService agenciaService;

    @GetMapping
    @Override
    public PaginaResponse<AgenciaResponse> listar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Boolean alerta,
            @RequestParam(required = false) String ordenarPor,
            @RequestParam(required = false) String direcao,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return agenciaService.listar(
                authorization,
                busca,
                ativo,
                alerta,
                ordenarPor,
                direcao,
                pagina,
                tamanho
        );
    }

    @GetMapping("/{id}/detalhe")
    @Override
    public DetalheAgenciaResponse detalhe(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable Long id) {
        return agenciaService.detalhar(
                authorization,
                id
        );
    }

    @PostMapping
    @Override
    public ResponseEntity<AgenciaResponse> criar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody CriarAgenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        agenciaService.criar(
                                authorization,
                                request
                        )
                );
    }

    @PutMapping("/{id}")
    @Override
    public AgenciaResponse atualizar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable Long id,
            @RequestBody AtualizarAgenciaRequest request) {
        return agenciaService.atualizar(
                authorization,
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> desativar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable Long id) {
        agenciaService.desativar(
                authorization,
                id
        );
        return ResponseEntity.noContent()
                .build();
    }
}
