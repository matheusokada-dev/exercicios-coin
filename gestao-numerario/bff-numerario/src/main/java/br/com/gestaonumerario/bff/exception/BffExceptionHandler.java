package br.com.gestaonumerario.bff.exception;

import br.com.gestaonumerario.bff.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class BffExceptionHandler {

    private static final int CAMPO_INVALIDO = 1000;
    private static final int SERVICO_INDISPONIVEL = 9000;
    private static final int ERRO_GENERICO = 1;

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<byte[]> handleApiError(RestClientResponseException exception) {
        HttpHeaders headers = new HttpHeaders();
        MediaType contentType = exception.getResponseHeaders() == null
                ? null
                : exception.getResponseHeaders().getContentType();
        headers.setContentType(contentType == null ? MediaType.APPLICATION_JSON : contentType);

        return new ResponseEntity<>(exception.getResponseBodyAsByteArray(), headers, exception.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException exception) {
        String field = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField())
                .orElse(null);
        return invalidField(field);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleParameterValidation(ConstraintViolationException exception) {
        String field = exception.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath().toString())
                .orElse(null);
        return invalidField(field);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return invalidField(exception.getName());
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnavailableApi(ResourceAccessException exception) {
        log.error("API Numerario indisponivel.", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(SERVICO_INDISPONIVEL, "Servico de dados indisponivel.", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception exception) {
        if (exception instanceof org.springframework.web.ErrorResponse webError) {
            int status = webError.getStatusCode().value();
            log.warn("Requisicao rejeitada pelo Spring MVC. status={}", status);
            return ResponseEntity.status(webError.getStatusCode())
                    .body(new ErrorResponse(status, messageFor(status), null));
        }

        log.error("Erro inesperado no BFF.", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ERRO_GENERICO, "Algo deu errado. Tente novamente mais tarde.", null));
    }

    private ResponseEntity<ErrorResponse> invalidField(String field) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(CAMPO_INVALIDO, "Campo obrigatorio ou invalido.", field));
    }

    private String messageFor(int status) {
        return switch (status) {
            case 400 -> "Requisicao invalida.";
            case 404 -> "Recurso nao encontrado.";
            case 405 -> "Metodo HTTP nao permitido.";
            case 415 -> "Formato de conteudo nao suportado.";
            default -> "Requisicao nao pode ser processada.";
        };
    }
}
