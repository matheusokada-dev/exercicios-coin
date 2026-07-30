package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.adapter.input.contract.OperacaoNumerarioApi;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.ConciliarOperacaoNumerarioRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.ExecutarOperacaoNumerarioRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.OcorrenciaOperacaoNumerarioRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.ProgramarOperacaoNumerarioRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.ReceberOperacaoNumerarioRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.VersaoOperacaoNumerarioRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.OperacaoNumerarioResponse;
import br.com.gestaonumerario.api.adapter.input.mapper.SolicitacaoNumerarioRestMapper;
import br.com.gestaonumerario.api.core.domain.model.command.ConciliarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ExecutarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ProgramarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ReceberOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.RegistrarOcorrenciaOperacaoCommand;
import br.com.gestaonumerario.api.port.input.OperacaoNumerarioInputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/solicitacoes-numerario")
@RequiredArgsConstructor
public class OperacaoNumerarioController implements OperacaoNumerarioApi {
    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    private final OperacaoNumerarioInputPort useCase;
    private final SolicitacaoNumerarioRestMapper mapper;

    @PutMapping("/{id}/programar")
    public OperacaoNumerarioResponse programar(
            @PathVariable Long id,
            @RequestBody ProgramarOperacaoNumerarioRequest r,
            @RequestHeader(IDEMPOTENCY_KEY) String key,
            @AuthenticationPrincipal UsuarioAutenticado u) {
        return mapper.toResponse(
                useCase.programar(
                        new ProgramarOperacaoNumerarioCommand(
                                id,
                                r.unidadeFaltanteId(),
                                r.versaoSolicitacao(),
                                u.id(),
                                key
                        )
                )
        );
    }

    @PutMapping("/{id}/iniciar-separacao")
    public OperacaoNumerarioResponse separar(
            @PathVariable Long id,
            @RequestBody VersaoOperacaoNumerarioRequest r,
            @AuthenticationPrincipal UsuarioAutenticado u) {
        return mapper.toResponse(
                useCase.iniciarSeparacao(
                        new ExecutarOperacaoNumerarioCommand(
                                id,
                                r.versaoOperacao(),
                                0,
                                u.id(),
                                null
                        )
                )
        );
    }

    @PutMapping("/{id}/expedir")
    public OperacaoNumerarioResponse expedir(
            @PathVariable Long id,
            @RequestBody ExecutarOperacaoNumerarioRequest r,
            @RequestHeader(IDEMPOTENCY_KEY) String key,
            @AuthenticationPrincipal UsuarioAutenticado u) {
        return mapper.toResponse(
                useCase.expedir(
                        new ExecutarOperacaoNumerarioCommand(
                                id,
                                r.versaoOperacao(),
                                r.versaoUnidade(),
                                u.id(),
                                key
                        )
                )
        );
    }

    @PutMapping("/{id}/registrar-ocorrencia")
    public OperacaoNumerarioResponse ocorrencia(
            @PathVariable Long id,
            @RequestBody OcorrenciaOperacaoNumerarioRequest r,
            @AuthenticationPrincipal UsuarioAutenticado u) {
        return mapper.toResponse(
                useCase.registrarOcorrencia(
                        new RegistrarOcorrenciaOperacaoCommand(
                                id,
                                r.descricao(),
                                r.versaoOperacao(),
                                u.id()
                        )
                )
        );
    }

    @PutMapping("/{id}/receber")
    public OperacaoNumerarioResponse receber(
            @PathVariable Long id,
            @RequestBody ReceberOperacaoNumerarioRequest r,
            @RequestHeader(IDEMPOTENCY_KEY) String key,
            @AuthenticationPrincipal UsuarioAutenticado u) {
        return mapper.toResponse(
                useCase.receber(
                        new ReceberOperacaoNumerarioCommand(
                                id,
                                r.valorRecebido(),
                                r.justificativaDivergencia(),
                                r.versaoOperacao(),
                                r.versaoUnidade(),
                                u.id(),
                                key
                        )
                )
        );
    }

    @PutMapping("/{id}/conciliar")
    public OperacaoNumerarioResponse conciliar(
            @PathVariable Long id,
            @RequestBody ConciliarOperacaoNumerarioRequest r,
            @RequestHeader(IDEMPOTENCY_KEY) String key,
            @AuthenticationPrincipal UsuarioAutenticado u) {
        return mapper.toResponse(
                useCase.conciliar(
                        new ConciliarOperacaoNumerarioCommand(
                                id,
                                r.justificativa(),
                                r.versaoOperacao(),
                                u.id(),
                                key
                        )
                )
        );
    }
}
