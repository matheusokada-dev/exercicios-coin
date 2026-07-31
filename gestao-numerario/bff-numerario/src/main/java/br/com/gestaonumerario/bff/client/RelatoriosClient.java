package br.com.gestaonumerario.bff.client;

import br.com.gestaonumerario.bff.dto.GerarRelatorioRequest;
import br.com.gestaonumerario.bff.dto.GerarRelatorioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class RelatoriosClient {

    private final RestClient relatoriosRestClient;

    public GerarRelatorioResponse gerar(GerarRelatorioRequest request) {
        return relatoriosRestClient.post()
                .uri("/v1/relatorios/gerar")
                .body(request)
                .retrieve()
                .body(GerarRelatorioResponse.class);
    }
}
