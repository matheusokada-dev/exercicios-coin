package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.dto.*;
import br.com.gestaonumerario.bff.service.ConsultaNumerarioService;
import br.com.gestaonumerario.bff.service.ComandoNumerarioService;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class ConsultaNumerarioController {
    private final ConsultaNumerarioService service;
    private final ComandoNumerarioService comandos;
    private static final String IDEMPOTENCY_KEY="Idempotency-Key";

    @GetMapping("/solicitacoes-numerario")
    public PaginaResponse<SolicitacaoNumerarioResponse> solicitacoes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestParam(required=false) Long agenciaId,
            @RequestParam(required=false) String tipo,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) Long origemId,
            @RequestParam(required=false) Long destinoId,
            @RequestParam(required=false) LocalDate dataInicio,
            @RequestParam(required=false) LocalDate dataFim,
            @RequestParam(defaultValue="0") @Min(0) int pagina,
            @RequestParam(defaultValue="20") @Min(1) @Max(100) int tamanho) {
        return service.solicitacoes(auth,agenciaId,tipo,status,origemId,destinoId,
                dataInicio,dataFim,pagina,tamanho);
    }

    @GetMapping("/solicitacoes-numerario/{id}")
    public DetalheSolicitacaoNumerarioResponse detalhe(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,@PathVariable Long id) {
        return service.detalhe(auth,id);
    }

    @GetMapping("/solicitacoes-numerario/{id}/historico")
    public List<HistoricoSolicitacaoResponse> historico(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,@PathVariable Long id) {
        return service.historico(auth,id);
    }

    @GetMapping("/unidades-operacionais")
    public List<UnidadeOperacionalResponse> unidades(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestParam(required=false) String tipo) {
        return service.unidades(auth,tipo);
    }

    @GetMapping("/operacoes-numerario")
    public PaginaResponse<OperacaoNumerarioResponse> operacoes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) Long origemId,
            @RequestParam(required=false) Long destinoId,
            @RequestParam(required=false) LocalDate dataInicio,
            @RequestParam(required=false) LocalDate dataFim,
            @RequestParam(defaultValue="0") @Min(0) int pagina,
            @RequestParam(defaultValue="20") @Min(1) @Max(100) int tamanho) {
        return service.operacoes(auth,status,origemId,destinoId,dataInicio,dataFim,pagina,tamanho);
    }

    @PostMapping("/solicitacoes-numerario")
    public org.springframework.http.ResponseEntity<SolicitacaoNumerarioResponse> criar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @jakarta.validation.Valid @RequestBody CriarSolicitacaoNumerarioRequest r) {
        return org.springframework.http.ResponseEntity.status(201).body(comandos.criar(auth,r));
    }
    @PutMapping("/solicitacoes-numerario/{id}/aprovar")
    public SolicitacaoNumerarioResponse aprovar(@RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @PathVariable Long id,@jakarta.validation.Valid @RequestBody DecidirSolicitacaoNumerarioRequest r){return comandos.decidir(a,id,"aprovar",r);}
    @PutMapping("/solicitacoes-numerario/{id}/rejeitar")
    public SolicitacaoNumerarioResponse rejeitar(@RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @PathVariable Long id,@jakarta.validation.Valid @RequestBody DecidirSolicitacaoNumerarioRequest r){return comandos.decidir(a,id,"rejeitar",r);}
    @PutMapping("/solicitacoes-numerario/{id}/cancelar")
    public SolicitacaoNumerarioResponse cancelar(@RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @PathVariable Long id,@jakarta.validation.Valid @RequestBody DecidirSolicitacaoNumerarioRequest r){return comandos.decidir(a,id,"cancelar",r);}
    @PutMapping("/solicitacoes-numerario/{id}/programar")
    public OperacaoNumerarioResponse programar(@RequestHeader(HttpHeaders.AUTHORIZATION) String a,@RequestHeader(IDEMPOTENCY_KEY) String k,
            @PathVariable Long id,@jakarta.validation.Valid @RequestBody ProgramarOperacaoNumerarioRequest r){return comandos.programar(a,id,k,r);}
    @PutMapping("/solicitacoes-numerario/{id}/iniciar-separacao")
    public OperacaoNumerarioResponse separar(@RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @PathVariable Long id,@jakarta.validation.Valid @RequestBody VersaoOperacaoNumerarioRequest r){return comandos.separar(a,id,r);}
    @PutMapping("/solicitacoes-numerario/{id}/expedir")
    public OperacaoNumerarioResponse expedir(@RequestHeader(HttpHeaders.AUTHORIZATION) String a,@RequestHeader(IDEMPOTENCY_KEY) String k,
            @PathVariable Long id,@jakarta.validation.Valid @RequestBody ExecutarOperacaoNumerarioRequest r){return comandos.expedir(a,id,k,r);}
    @PutMapping("/solicitacoes-numerario/{id}/registrar-ocorrencia")
    public OperacaoNumerarioResponse ocorrencia(@RequestHeader(HttpHeaders.AUTHORIZATION) String a,
            @PathVariable Long id,@jakarta.validation.Valid @RequestBody OcorrenciaOperacaoNumerarioRequest r){return comandos.ocorrencia(a,id,r);}
    @PutMapping("/solicitacoes-numerario/{id}/receber")
    public OperacaoNumerarioResponse receber(@RequestHeader(HttpHeaders.AUTHORIZATION) String a,@RequestHeader(IDEMPOTENCY_KEY) String k,
            @PathVariable Long id,@jakarta.validation.Valid @RequestBody ReceberOperacaoNumerarioRequest r){return comandos.receber(a,id,k,r);}
    @PutMapping("/solicitacoes-numerario/{id}/conciliar")
    public OperacaoNumerarioResponse conciliar(@RequestHeader(HttpHeaders.AUTHORIZATION) String a,@RequestHeader(IDEMPOTENCY_KEY) String k,
            @PathVariable Long id,@jakarta.validation.Valid @RequestBody ConciliarOperacaoNumerarioRequest r){return comandos.conciliar(a,id,k,r);}
    @PostMapping("/tesouraria/carga-inicial")
    public UnidadeOperacionalResponse carga(@RequestHeader(HttpHeaders.AUTHORIZATION) String a,@RequestHeader(IDEMPOTENCY_KEY) String k,
            @jakarta.validation.Valid @RequestBody CargaInicialTesourariaRequest r){return comandos.carga(a,k,r);}
    @PostMapping("/solicitacoes-numerario/{id}/ajustes-divergencia")
    public UnidadeOperacionalResponse ajustar(@RequestHeader(HttpHeaders.AUTHORIZATION) String a,@RequestHeader(IDEMPOTENCY_KEY) String k,
            @PathVariable Long id,@jakarta.validation.Valid @RequestBody AjustarDivergenciaRequest r){return comandos.ajustar(a,id,k,r);}
}
