package br.com.gestaonumerario.bff.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ApiNumerarioClientTest {

    private MockRestServiceServer server;
    private ApiNumerarioClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://api-numerario");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ApiNumerarioClient(builder.build());
    }

    @Test
    void encaminhaJwtEFiltrosParaApi() {
        server.expect(once(), requestTo("http://api-numerario/api/v1/agencias?busca=centro&alerta=true"
                        + "&ordenarPor=saldoAtual&direcao=desc&pagina=1&tamanho=10"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess("""
                        {"itens":[],"pagina":1,"tamanho":10,"totalItens":0,"totalPaginas":0}
                        """, MediaType.APPLICATION_JSON));

        var response = client.listarAgencias(
                "Bearer token", "centro", null, true, "saldoAtual", "desc", 1, 10);

        assertThat(response.pagina()).isEqualTo(1);
        assertThat(response.itens()).isEmpty();
        server.verify();
    }
}
