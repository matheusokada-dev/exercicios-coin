package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.contract.RelatorioApi;
import br.com.gestaonumerario.bff.dto.GerarLivroCaixaRequest;
import br.com.gestaonumerario.bff.dto.GerarRelatorioResponse;
import br.com.gestaonumerario.bff.service.RelatorioLivroCaixaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
public class RelatorioController implements RelatorioApi {

    private final RelatorioLivroCaixaService relatorioLivroCaixaService;

    @PostMapping("/livro-caixa")
    @Override
    public GerarRelatorioResponse gerarLivroCaixa(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody GerarLivroCaixaRequest request) {
        return relatorioLivroCaixaService.gerar(authorization, request);
    }
}
