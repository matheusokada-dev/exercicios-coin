package br.com.gestaonumerario.bff.exception;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;

class BffExceptionHandlerTest {

    @Test
    void preservaStatusEPayloadDeErroDaApi() {
        byte[] body = "{\"codError\":3003,\"msgError\":\"Solicitacao duplicada.\"}".getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var exception = HttpClientErrorException.create(
                HttpStatus.CONFLICT,
                "Conflict",
                headers,
                body,
                StandardCharsets.UTF_8
        );

        var response = new BffExceptionHandler().handleApiError(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(
                response.getHeaders()
                        .getContentType()
        ).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(
                new String(
                        response.getBody(),
                        StandardCharsets.UTF_8
                )
        ).isEqualTo(
                new String(
                        body,
                        StandardCharsets.UTF_8
                )
        );
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
        assertThat(
                response.getBody()
                        .codError()
        ).isEqualTo(9001);
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
        assertThat(
                response.getBody()
                        .codError()
        ).isEqualTo(9000);
    }

    @Test
    void retornaCampoInvalidoQuandoTipoDoParametroNaoConfere() {
        var exception = new MethodArgumentTypeMismatchException(
                "texto",
                Long.class,
                "agenciaId",
                null,
                null
        );

        var response = new BffExceptionHandler().handleTypeMismatch(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(
                response.getBody()
                        .codError()
        ).isEqualTo(1000);
        assertThat(
                response.getBody()
                        .value()
        ).isEqualTo("agenciaId");
    }

    @Test
    void retornaCampoInvalidoQuandoCabecalhoObrigatorioNaoFoiEnviado() {
        var exception = new MissingRequestHeaderException(
                "Authorization",
                null
        );

        var response = new BffExceptionHandler().handleMissingHeader(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(
                response.getBody()
                        .value()
        ).isEqualTo("Authorization");
    }

    @Test
    void converteErroInesperadoEmRespostaInternaSemExporDetalhes() {
        var response = new BffExceptionHandler().handleUnexpectedError(new IllegalStateException("detalhe interno"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(
                response.getBody()
                        .codError()
        ).isEqualTo(1);
        assertThat(
                response.getBody()
                        .msgError()
        ).isEqualTo("Algo deu errado. Tente novamente mais tarde.");
    }
}
