package br.com.gestaonumerario.bff.config;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void preservaCorrelationIdValidoDuranteARequisicaoELimpaContextoAoFinal() throws Exception {
        String correlationId = UUID.randomUUID()
                .toString();
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var correlationIdDuranteFiltro = new AtomicReference<String>();
        request.addHeader(
                CorrelationIdFilter.HEADER,
                " " + correlationId + " "
        );

        filter.doFilter(
                request,
                response,
                (ignoredRequest, ignoredResponse) -> correlationIdDuranteFiltro
                        .set(MDC.get(CorrelationIdFilter.MDC_KEY))
        );

        assertThat(correlationIdDuranteFiltro).hasValue(correlationId);
        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo(correlationId);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void geraCorrelationIdQuandoCabecalhoNaoExiste() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                (ignoredRequest, ignoredResponse) -> {
                }
        );

        assertThat(response.getHeader(CorrelationIdFilter.HEADER))
                .satisfies(value -> assertThat(UUID.fromString(value)).isNotNull());
    }

    @Test
    void substituiCorrelationIdMalformado() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.addHeader(
                CorrelationIdFilter.HEADER,
                "nao-e-uuid"
        );

        filter.doFilter(
                request,
                response,
                (ignoredRequest, ignoredResponse) -> {
                }
        );

        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isNotEqualTo("nao-e-uuid")
                .satisfies(value -> assertThat(UUID.fromString(value)).isNotNull());
    }
}
