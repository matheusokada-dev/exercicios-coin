package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.adapter.input.contract.FinanceiroNumerarioApi;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AjustarDivergenciaRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.CargaInicialTesourariaRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.UnidadeOperacionalResponse;
import br.com.gestaonumerario.api.core.domain.model.UnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.command.AjustarDivergenciaCommand;
import br.com.gestaonumerario.api.core.domain.model.command.CargaInicialTesourariaCommand;
import br.com.gestaonumerario.api.port.input.FinanceiroNumerarioInputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FinanceiroNumerarioController implements FinanceiroNumerarioApi {
    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    private final FinanceiroNumerarioInputPort useCase;

    @PostMapping("/tesouraria/carga-inicial")
    public UnidadeOperacionalResponse cargaInicial(
            @RequestBody CargaInicialTesourariaRequest r,
            @RequestHeader(IDEMPOTENCY_KEY) String key,
            @AuthenticationPrincipal UsuarioAutenticado u) {
        return resposta(
                useCase.realizarCargaInicial(
                        new CargaInicialTesourariaCommand(
                                r.valor(),
                                r.justificativa(),
                                r.versaoUnidade(),
                                u.id(),
                                key
                        )
                )
        );
    }

    @PostMapping("/solicitacoes-numerario/{id}/ajustes-divergencia")
    public UnidadeOperacionalResponse ajustar(
            @PathVariable Long id,
            @RequestBody AjustarDivergenciaRequest r,
            @RequestHeader(IDEMPOTENCY_KEY) String key,
            @AuthenticationPrincipal UsuarioAutenticado u) {
        return resposta(
                useCase.ajustarDivergencia(
                        new AjustarDivergenciaCommand(
                                id,
                                r.unidadeId(),
                                r.valor(),
                                r.entrada(),
                                r.justificativa(),
                                r.versaoUnidade(),
                                u.id(),
                                key
                        )
                )
        );
    }

    private static UnidadeOperacionalResponse resposta(UnidadeOperacional u) {
        return new UnidadeOperacionalResponse(
                u.getId(),
                u.getTipo(),
                u.getCodigo(),
                u.getNome(),
                u.getSaldoAtual(),
                u.getVersao(),
                u.getAtualizadoEm()
        );
    }
}
