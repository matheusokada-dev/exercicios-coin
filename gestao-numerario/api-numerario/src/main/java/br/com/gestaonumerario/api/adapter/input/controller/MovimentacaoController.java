package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.contract.MovimentacaoApi;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.MovimentacaoResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.PaginaResponse;
import br.com.gestaonumerario.api.adapter.input.mapper.MovimentacaoRestMapper;
import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.FiltroMovimentacao;
import br.com.gestaonumerario.api.port.input.MovimentacaoInputPort;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/movimentacoes")
@RequiredArgsConstructor
@Validated
public class MovimentacaoController implements MovimentacaoApi {

    private final MovimentacaoInputPort movimentacaoUseCase;
    private final MovimentacaoRestMapper mapper;

    @GetMapping
    @Override
    public PaginaResponse<MovimentacaoResponse> consultar(
            @RequestParam(required = false) Long agenciaId,
            @RequestParam(required = false) TipoMovimentacao tipo,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        var resultado = movimentacaoUseCase.consultar(
                new FiltroMovimentacao(
                        agenciaId,
                        tipo,
                        dataInicio,
                        dataFim,
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
}
