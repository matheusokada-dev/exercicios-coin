package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.contract.ConsultaNumerarioApi;
import br.com.gestaonumerario.bff.dto.AjustarDivergenciaRequest;
import br.com.gestaonumerario.bff.dto.CargaInicialTesourariaRequest;
import br.com.gestaonumerario.bff.dto.ConciliarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.CriarSolicitacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.DecidirSolicitacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.DetalheSolicitacaoNumerarioResponse;
import br.com.gestaonumerario.bff.dto.ExecutarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.HistoricoSolicitacaoResponse;
import br.com.gestaonumerario.bff.dto.OcorrenciaOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.OperacaoNumerarioResponse;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.dto.ProgramarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.ReceberOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.SolicitacaoNumerarioResponse;
import br.com.gestaonumerario.bff.dto.UnidadeOperacionalResponse;
import br.com.gestaonumerario.bff.dto.VersaoOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.service.ComandoNumerarioService;
import br.com.gestaonumerario.bff.service.ConsultaNumerarioService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class ConsultaNumerarioController implements ConsultaNumerarioApi {
    private final ConsultaNumerarioService service;
    private final ComandoNumerarioService comandos;
    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    @GetMapping("/solicitacoes-numerario")
    @Override
    public PaginaResponse<SolicitacaoNumerarioResponse> solicitacoes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestParam(required = false) Long agenciaId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long origemId,
            @RequestParam(required = false) Long destinoId,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.solicitacoes(
                auth,
                agenciaId,
                tipo,
                status,
                origemId,
                destinoId,
                dataInicio,
                dataFim,
                pagina,
                tamanho
        );
    }

    @GetMapping("/solicitacoes-numerario/{id}")
    @Override
    public DetalheSolicitacaoNumerarioResponse detalhe(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @PathVariable Long id) {
        return service.detalhe(
                auth,
                id
        );
    }

    @GetMapping("/solicitacoes-numerario/{id}/historico")
    @Override
    public List<HistoricoSolicitacaoResponse> historico(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @PathVariable Long id) {
        return service.historico(
                auth,
                id
        );
    }

    @GetMapping("/unidades-operacionais")
    @Override
    public List<UnidadeOperacionalResponse> unidades(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestParam(required = false) String tipo) {
        return service.unidades(
                auth,
                tipo
        );
    }

    @GetMapping("/operacoes-numerario")
    @Override
    public PaginaResponse<OperacaoNumerarioResponse> operacoes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long origemId,
            @RequestParam(required = false) Long destinoId,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.operacoes(
                auth,
                status,
                origemId,
                destinoId,
                dataInicio,
                dataFim,
                pagina,
                tamanho
        );
    }

    @PostMapping("/solicitacoes-numerario")
    @Override
    public org.springframework.http.ResponseEntity<SolicitacaoNumerarioResponse> criar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody CriarSolicitacaoNumerarioRequest r) {
        return org.springframework.http.ResponseEntity.status(201)
                .body(
                        comandos.criar(
                                auth,
                                r
                        )
                );
    }

    @PutMapping("/solicitacoes-numerario/{id}/aprovar")
    @Override
    public SolicitacaoNumerarioResponse aprovar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @PathVariable Long id,
            @RequestBody DecidirSolicitacaoNumerarioRequest r) {
        return comandos.decidir(
                a,
                id,
                "aprovar",
                r
        );
    }

    @PutMapping("/solicitacoes-numerario/{id}/rejeitar")
    @Override
    public SolicitacaoNumerarioResponse rejeitar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @PathVariable Long id,
            @RequestBody DecidirSolicitacaoNumerarioRequest r) {
        return comandos.decidir(
                a,
                id,
                "rejeitar",
                r
        );
    }

    @PutMapping("/solicitacoes-numerario/{id}/cancelar")
    @Override
    public SolicitacaoNumerarioResponse cancelar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @PathVariable Long id,
            @RequestBody DecidirSolicitacaoNumerarioRequest r) {
        return comandos.decidir(
                a,
                id,
                "cancelar",
                r
        );
    }

    @PutMapping("/solicitacoes-numerario/{id}/programar")
    @Override
    public OperacaoNumerarioResponse programar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @RequestHeader(IDEMPOTENCY_KEY) String k,
            @PathVariable Long id,
            @RequestBody ProgramarOperacaoNumerarioRequest r) {
        return comandos.programar(
                a,
                id,
                k,
                r
        );
    }

    @PutMapping("/solicitacoes-numerario/{id}/iniciar-separacao")
    @Override
    public OperacaoNumerarioResponse separar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @PathVariable Long id,
            @RequestBody VersaoOperacaoNumerarioRequest r) {
        return comandos.separar(
                a,
                id,
                r
        );
    }

    @PutMapping("/solicitacoes-numerario/{id}/expedir")
    @Override
    public OperacaoNumerarioResponse expedir(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @RequestHeader(IDEMPOTENCY_KEY) String k,
            @PathVariable Long id,
            @RequestBody ExecutarOperacaoNumerarioRequest r) {
        return comandos.expedir(
                a,
                id,
                k,
                r
        );
    }

    @PutMapping("/solicitacoes-numerario/{id}/registrar-ocorrencia")
    @Override
    public OperacaoNumerarioResponse ocorrencia(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @PathVariable Long id,
            @RequestBody OcorrenciaOperacaoNumerarioRequest r) {
        return comandos.ocorrencia(
                a,
                id,
                r
        );
    }

    @PutMapping("/solicitacoes-numerario/{id}/receber")
    @Override
    public OperacaoNumerarioResponse receber(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @RequestHeader(IDEMPOTENCY_KEY) String k,
            @PathVariable Long id,
            @RequestBody ReceberOperacaoNumerarioRequest r) {
        return comandos.receber(
                a,
                id,
                k,
                r
        );
    }

    @PutMapping("/solicitacoes-numerario/{id}/conciliar")
    @Override
    public OperacaoNumerarioResponse conciliar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @RequestHeader(IDEMPOTENCY_KEY) String k,
            @PathVariable Long id,
            @RequestBody ConciliarOperacaoNumerarioRequest r) {
        return comandos.conciliar(
                a,
                id,
                k,
                r
        );
    }

    @PostMapping("/tesouraria/carga-inicial")
    @Override
    public UnidadeOperacionalResponse carga(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @RequestHeader(IDEMPOTENCY_KEY) String k,
            @RequestBody CargaInicialTesourariaRequest r) {
        return comandos.carga(
                a,
                k,
                r
        );
    }

    @PostMapping("/solicitacoes-numerario/{id}/ajustes-divergencia")
    @Override
    public UnidadeOperacionalResponse ajustar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @RequestHeader(IDEMPOTENCY_KEY) String k,
            @PathVariable Long id,
            @RequestBody AjustarDivergenciaRequest r) {
        return comandos.ajustar(
                a,
                id,
                k,
                r
        );
    }
}
