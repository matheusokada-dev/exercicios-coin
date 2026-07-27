package br.com.gestaonumerario.api.adapter.input.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = normalizar(request.getHeader(HEADER));

        //Mapped Diagnostic Context -
        MDC.put(MDC_KEY, correlationId);
        //Adiciona o HEADER na resposta para que o cliente possa rastrear a requisição.
        response.setHeader(HEADER, correlationId);

        try {
            //Passa a requisição para o próximo filtro na cadeia.
            filterChain.doFilter(request, response);
        } finally {
            //Garante a limpeza dos logs mesmo se houver uma exceção no processamento da requisição.
            MDC.remove(MDC_KEY);
        }
    }

    private String normalizar(String recebido) {
        if (recebido != null) {
            try {
                // Se tiver HEADER, nós normalizamos e devolvemos.
                return UUID.fromString(recebido.strip()).toString();
            } catch (IllegalArgumentException ignored) {
                // Um valor externo malformado nao deve contaminar logs ou headers.
            }
        }
        //Se não chegar um HEADER, a gente gera um aleatório.
        return UUID.randomUUID().toString();
    }
}
