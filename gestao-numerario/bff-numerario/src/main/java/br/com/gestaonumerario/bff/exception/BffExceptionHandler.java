package br.com.gestaonumerario.bff.exception;

import br.com.gestaonumerario.bff.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
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
    private static final int TIMEOUT_SERVICO = 9001;
    private static final int RELATORIO_SEM_DADOS = 2000;
    private static final int PERIODO_INVALIDO = 2001;
    private static final int ERRO_GENERICO = 1;

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<byte[]> handleApiError(RestClientResponseException exception) {
        HttpHeaders headers = new HttpHeaders();
        MediaType contentType = exception.getResponseHeaders() == null
                ? null
                : exception.getResponseHeaders()
                        .getContentType();
        headers.setContentType(contentType == null ? MediaType.APPLICATION_JSON : contentType);

        return new ResponseEntity<>(
                exception.getResponseBodyAsByteArray(),
                headers,
                exception.getStatusCode()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException exception) {
        return exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(
                        error -> invalidField(
                                error.getField(),
                                validationMessage(
                                        error.getField(),
                                        error.getCode(),
                                        error.getDefaultMessage()
                                )
                        )
                )
                .orElseGet(
                        () -> invalidField(
                                null,
                                "O corpo da requisição é inválido."
                        )
                );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleParameterValidation(ConstraintViolationException exception) {
        String field = exception.getConstraintViolations()
                .stream()
                .findFirst()
                .map(
                        violation -> violation.getPropertyPath()
                                .toString()
                )
                .orElse(null);
        return invalidField(
                field,
                "O parâmetro '" + field + "' é inválido."
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String field = exception.getName();
        return invalidField(
                field,
                "O valor informado para '" + field + "' é inválido."
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException exception) {
        String header = exception.getHeaderName();
        return invalidField(
                header,
                "O cabeçalho '" + header + "' é obrigatório."
        );
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnavailableApi(ResourceAccessException exception) {
        if (possuiCausaTimeout(exception)) {
            log.error(
                    "Timeout ao acessar API Numerario.",
                    exception
            );
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body(
                            new ErrorResponse(
                                    TIMEOUT_SERVICO,
                                    "Tempo limite do servico de dados excedido.",
                                    null
                            )
                    );
        }
        log.error(
                "API Numerario indisponivel.",
                exception
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        new ErrorResponse(
                                SERVICO_INDISPONIVEL,
                                "Servico de dados indisponivel.",
                                null
                        )
                );
    }

    @ExceptionHandler(RelatorioSemDadosException.class)
    public ResponseEntity<ErrorResponse> handleRelatorioSemDados(RelatorioSemDadosException exception) {
        return ResponseEntity.unprocessableEntity()
                .body(new ErrorResponse(RELATORIO_SEM_DADOS, exception.getMessage(), null));
    }

    @ExceptionHandler(PeriodoRelatorioInvalidoException.class)
    public ResponseEntity<ErrorResponse> handlePeriodoInvalido(PeriodoRelatorioInvalidoException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(PERIODO_INVALIDO, exception.getMessage(), "dataFim"));
    }

    private boolean possuiCausaTimeout(Throwable exception) {
        Throwable causa = exception;
        while (causa != null) {
            if (causa instanceof java.net.http.HttpTimeoutException
                    || causa instanceof java.net.SocketTimeoutException) {
                return true;
            }
            causa = causa.getCause();
        }
        return false;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception exception) {
        if (exception instanceof org.springframework.web.ErrorResponse webError) {
            int status = webError.getStatusCode()
                    .value();
            log.warn(
                    "Requisicao rejeitada pelo Spring MVC. status={}",
                    status
            );
            return ResponseEntity.status(webError.getStatusCode())
                    .body(
                            new ErrorResponse(
                                    status,
                                    messageFor(status),
                                    null
                            )
                    );
        }

        log.error(
                "Erro inesperado no BFF.",
                exception
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ErrorResponse(
                                ERRO_GENERICO,
                                "Algo deu errado. Tente novamente mais tarde.",
                                null
                        )
                );
    }

    private ResponseEntity<ErrorResponse> invalidField(String field, String message) {
        return ResponseEntity.badRequest()
                .body(
                        new ErrorResponse(
                                CAMPO_INVALIDO,
                                message,
                                field
                        )
                );
    }

    private String validationMessage(String field, String code, String defaultMessage) {
        return switch (code == null ? "" : code) {
            case "NotNull", "NotBlank", "NotEmpty" -> "O campo '" + field + "' é obrigatório.";
            case "Positive" -> "O campo '" + field + "' deve ser maior que zero.";
            case "PositiveOrZero" -> "O campo '" + field + "' não pode ser negativo.";
            case "Digits" -> "O campo '" + field + "' possui precisão ou casas decimais inválidas.";
            case "Size" -> "O campo '" + field + "' possui tamanho inválido.";
            case "Future", "FutureOrPresent" -> "O campo '" + field + "' não pode estar no passado.";
            default -> defaultMessage == null ? "Valor inválido." : defaultMessage;
        };
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
