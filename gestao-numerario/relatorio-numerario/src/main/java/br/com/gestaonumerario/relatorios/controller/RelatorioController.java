package br.com.gestaonumerario.relatorios.controller;

import br.com.gestaonumerario.relatorios.contract.RelatorioApi;
import br.com.gestaonumerario.relatorios.dto.GerarRelatorioRequest;
import br.com.gestaonumerario.relatorios.dto.GerarRelatorioResponse;
import br.com.gestaonumerario.relatorios.service.GerarRelatorioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/relatorios")
public class RelatorioController implements RelatorioApi {

    private final GerarRelatorioService service;

    public RelatorioController(GerarRelatorioService service) {
        this.service = service;
    }

    @PostMapping("/gerar")
    @Override
    public GerarRelatorioResponse gerar(@Valid @RequestBody GerarRelatorioRequest request) {
        return service.gerar(request);
    }
}
