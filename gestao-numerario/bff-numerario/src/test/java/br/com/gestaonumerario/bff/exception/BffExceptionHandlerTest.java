package br.com.gestaonumerario.bff.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class BffExceptionHandlerTest {

    @Test
    void preservaStatusEPayloadDeErroDaApi() {
        byte[] body = "{\"codError\":3003,\"msgError\":\"Solicitacao duplicada.\"}"
                .getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var exception = HttpClientErrorException.create(
                HttpStatus.CONFLICT, "Conflict", headers, body, StandardCharsets.UTF_8);

        var response = new BffExceptionHandler().handleApiError(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(new String(response.getBody(), StandardCharsets.UTF_8))
                .isEqualTo(new String(body, StandardCharsets.UTF_8));
    }

    @Test
    void retornaGatewayTimeoutQuandoApiExcedeTempoLimite() {
        var exception = new ResourceAccessException(
                "timeout",
                new java.net.http.HttpTimeoutException("tempo excedido")
        );

        var response = new BffExceptionHandler().handleUnavailableApi(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().codError()).isEqualTo(9001);
    }

    @Test
    void retornaServiceUnavailableQuandoConexaoFalha() {
        var exception = new ResourceAccessException(
                "conexao recusada",
                new java.net.ConnectException("recusada")
        );

        var response = new BffExceptionHandler().handleUnavailableApi(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().codError()).isEqualTo(9000);
    }
}
