package br.com.gestaonumerario.api.adapter.input.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public void escrever(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            int codigo,
            String mensagem) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                ErrorObject.builder()
                        .codError(codigo)
                        .msgError(mensagem)
                        .timestamp(Instant.now())
                        .status(status)
                        .error(status == HttpServletResponse.SC_UNAUTHORIZED ? "Unauthorized" : "Forbidden")
                        .code(status == HttpServletResponse.SC_UNAUTHORIZED ? "NAO_AUTENTICADO" : "ACESSO_NEGADO")
                        .message(mensagem)
                        .path(request.getRequestURI())
                        .build()
        );
    }
}
