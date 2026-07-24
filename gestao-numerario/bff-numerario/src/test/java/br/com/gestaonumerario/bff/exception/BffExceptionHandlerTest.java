package br.com.gestaonumerario.bff.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

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
}
