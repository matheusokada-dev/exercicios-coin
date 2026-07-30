package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.contract.ConsultaNumerarioApi;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.OperacaoNumerarioResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.PaginaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.UnidadeOperacionalResponse;
import br.com.gestaonumerario.api.adapter.input.mapper.SolicitacaoNumerarioRestMapper;
import br.com.gestaonumerario.api.core.domain.enums.StatusOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.FiltroOperacaoNumerario;
import br.com.gestaonumerario.api.port.input.ConsultaNumerarioInputPort;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class ConsultaNumerarioController implements ConsultaNumerarioApi {
    private final ConsultaNumerarioInputPort useCase;
    private final SolicitacaoNumerarioRestMapper mapper;

    @GetMapping("/unidades-operacionais")
    @Override
    public List<UnidadeOperacionalResponse> unidades(@RequestParam(required = false) TipoUnidadeOperacional tipo) {
        return useCase.consultarUnidades(tipo)
                .stream()
                .map(
                        u -> new UnidadeOperacionalResponse(
                                u.getId(),
                                u.getTipo(),
                                u.getCodigo(),
                                u.getNome(),
                                u.getSaldoAtual(),
                                u.getVersao(),
                                u.getAtualizadoEm()
                        )
                )
                .toList();
    }

    @GetMapping("/operacoes-numerario")
    @Override
    public PaginaResponse<OperacaoNumerarioResponse> operacoes(
            @RequestParam(required = false) StatusOperacaoNumerario status,
            @RequestParam(required = false) Long origemId,
            @RequestParam(required = false) Long destinoId,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        var p = useCase.consultarOperacoes(
                new FiltroOperacaoNumerario(
                        status,
                        origemId,
                        destinoId,
                        dataInicio,
                        dataFim,
                        pagina,
                        tamanho
                )
        );
        return new PaginaResponse<>(
                p.itens()
                        .stream()
                        .map(mapper::toResponse)
                        .toList(),
                p.pagina(),
                p.tamanho(),
                p.totalItens(),
                p.totalPaginas()
        );
    }
}
