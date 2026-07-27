package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.adapter.input.contract.SolicitacaoNumerarioApi;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.*;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.*;
import br.com.gestaonumerario.api.adapter.input.mapper.SolicitacaoNumerarioRestMapper;
import br.com.gestaonumerario.api.core.domain.enums.*;
import br.com.gestaonumerario.api.core.domain.model.FiltroSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.command.*;
import br.com.gestaonumerario.api.port.input.SolicitacaoNumerarioInputPort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/solicitacoes-numerario")
@RequiredArgsConstructor @Validated
public class SolicitacaoNumerarioController implements SolicitacaoNumerarioApi {
    private final SolicitacaoNumerarioInputPort useCase;
    private final SolicitacaoNumerarioRestMapper mapper;

    @PostMapping
    public ResponseEntity<SolicitacaoNumerarioResponse> criar(
            @RequestBody CriarSolicitacaoNumerarioRequest r,
            @AuthenticationPrincipal UsuarioAutenticado u) {
        var c=new CriarSolicitacaoNumerarioCommand(r.tipoOperacao(),r.agenciaId(),
                r.valor(),r.motivo(),r.dataDesejada(),u.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(useCase.criar(c)));
    }
    @GetMapping
    public PaginaResponse<SolicitacaoNumerarioResponse> consultar(
            @RequestParam(required=false) Long agenciaId,
            @RequestParam(required=false) TipoOperacaoNumerario tipo,
            @RequestParam(required=false) StatusSolicitacaoNumerario status,
            @RequestParam(required=false) Long origemId,
            @RequestParam(required=false) Long destinoId,
            @RequestParam(required=false) LocalDate dataInicio,
            @RequestParam(required=false) LocalDate dataFim,
            @RequestParam(defaultValue="0") int pagina,
            @RequestParam(defaultValue="20") int tamanho) {
        var p=useCase.consultar(new FiltroSolicitacaoNumerario(agenciaId,tipo,status,
                origemId,destinoId,dataInicio,dataFim,pagina,tamanho));
        return new PaginaResponse<>(p.itens().stream().map(mapper::toResponse).toList(),
                p.pagina(),p.tamanho(),p.totalItens(),p.totalPaginas());
    }
    @GetMapping("/{id}")
    public DetalheSolicitacaoNumerarioResponse detalhar(@PathVariable Long id) {
        return mapper.toResponse(useCase.detalhar(id));
    }
    @GetMapping("/{id}/historico")
    public java.util.List<DetalheSolicitacaoNumerarioResponse.HistoricoResumo> historico(
            @PathVariable Long id) {
        return mapper.toResponse(useCase.detalhar(id)).historico();
    }
    @PutMapping("/{id}/aprovar")
    public SolicitacaoNumerarioResponse aprovar(@PathVariable Long id,
            @RequestBody DecidirSolicitacaoNumerarioRequest r,
            @AuthenticationPrincipal UsuarioAutenticado u) {
        return mapper.toResponse(useCase.aprovar(comando(id,r,u)));
    }
    @PutMapping("/{id}/rejeitar")
    public SolicitacaoNumerarioResponse rejeitar(@PathVariable Long id,
            @RequestBody DecidirSolicitacaoNumerarioRequest r,
            @AuthenticationPrincipal UsuarioAutenticado u) {
        return mapper.toResponse(useCase.rejeitar(comando(id,r,u)));
    }
    @PutMapping("/{id}/cancelar")
    public SolicitacaoNumerarioResponse cancelar(@PathVariable Long id,
            @RequestBody DecidirSolicitacaoNumerarioRequest r,
            @AuthenticationPrincipal UsuarioAutenticado u) {
        return mapper.toResponse(useCase.cancelar(comando(id,r,u)));
    }
    private static DecidirSolicitacaoNumerarioCommand comando(Long id,
            DecidirSolicitacaoNumerarioRequest r,UsuarioAutenticado u) {
        return new DecidirSolicitacaoNumerarioCommand(id,r.justificativa(),r.versao(),u.id());
    }
}
