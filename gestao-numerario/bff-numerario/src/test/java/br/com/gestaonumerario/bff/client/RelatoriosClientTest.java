package br.com.gestaonumerario.bff.client;

import br.com.gestaonumerario.bff.dto.GerarRelatorioRequest;
import br.com.gestaonumerario.bff.dto.MetadadosRelatorio;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RelatoriosClientTest {

    @Test
    void deveChamarEndpointDocumentadoEDesserializarBase64() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://relatorios");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RelatoriosClient client = new RelatoriosClient(builder.build());
        server.expect(once(), requestTo("http://relatorios/v1/relatorios/gerar"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(jsonPath("$.colunas[0]").value("Código"))
                .andExpect(jsonPath("$.linhas[0][1]").value(100.50))
                .andExpect(jsonPath("$.metadados.nomeArquivo").value("teste"))
                .andExpect(jsonPath("$.formato").value("xlsx"))
                .andRespond(withSuccess(
                        """
                                {
                                  "conteudo":"UEsDBA==",
                                  "nomeArquivo":"teste.xlsx",
                                  "formato":"xlsx",
                                  "dataGeracao":"2026-07-29T14:00:00"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));

        var response = client.gerar(new GerarRelatorioRequest(
                List.of("Código", "Valor"),
                List.of(List.of("C001", new java.math.BigDecimal("100.50"))),
                new MetadadosRelatorio("Teste", null, null, "gestor", "teste"),
                "xlsx"
        ));

        assertThat(response.conteudo()).isEqualTo("UEsDBA==");
        assertThat(response.nomeArquivo()).isEqualTo("teste.xlsx");
        server.verify();
    }
}
