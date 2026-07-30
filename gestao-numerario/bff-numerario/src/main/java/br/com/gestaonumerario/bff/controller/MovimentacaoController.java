package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.contract.MovimentacaoApi;
import br.com.gestaonumerario.bff.dto.MovimentacaoResponse;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.service.MovimentacaoService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/movimentacoes")
@RequiredArgsConstructor
public class MovimentacaoController implements MovimentacaoApi {

    private final MovimentacaoService movimentacaoService;

    @GetMapping
    @Override
    public PaginaResponse<MovimentacaoResponse> listar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(required = false) Long agenciaId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return movimentacaoService.listar(
                authorization,
                agenciaId,
                tipo,
                dataInicio,
                dataFim,
                pagina,
                tamanho
        );
    }

}
