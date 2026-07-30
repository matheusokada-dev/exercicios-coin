package br.com.gestaonumerario.api.adapter.input.error;

import br.com.gestaonumerario.api.core.exception.BaseException;
import br.com.gestaonumerario.api.core.exception.CredenciaisInvalidasException;
import br.com.gestaonumerario.api.core.exception.ErrorEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BaseExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorObject<Void>> handleConflitoOtimista(
            ObjectOptimisticLockingFailureException exception,
            HttpServletRequest request) {
        return resposta(
                ErrorEnum.CONFLITO_VERSAO,
                null,
                null,
                request
        );
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErrorObject<DetalheFalhaAutenticacao>> handleCredenciaisInvalidas(
            CredenciaisInvalidasException exception,
            HttpServletRequest request) {
        DetalheFalhaAutenticacao detalhe = exception.getTentativasRestantes() == null
                ? null
                : new DetalheFalhaAutenticacao(
                        exception.getTentativasRestantes(),
                        exception.getBloqueadoAte()
                );
        return resposta(
                exception.getErrorEnum(),
                detalhe,
                null,
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorObject<String>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        String campo = exception.getName();
        String mensagem = "O valor informado para '" + campo + "' é inválido.";
        return resposta(
                ErrorEnum.CAMPO_INVALIDO,
                campo,
                List.of(
                        new FieldErrorObject(
                                campo,
                                mensagem
                        )
                ),
                request
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorObject<String>> handleCabecalhoAusente(
            MissingRequestHeaderException exception,
            HttpServletRequest request) {
        String campo = exception.getHeaderName();
        String mensagem = "O cabeçalho '" + campo + "' é obrigatório.";
        return resposta(
                ErrorEnum.CAMPO_OBRIGATORIO,
                campo,
                List.of(
                        new FieldErrorObject(
                                campo,
                                mensagem
                        )
                ),
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorObject<Void>> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        List<FieldErrorObject> fields = exception.getConstraintViolations()
                .stream()
                .map(this::paraErroDeCampo)
                .toList();
        return resposta(
                ErrorEnum.CAMPO_INVALIDO,
                null,
                fields,
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorObject<String>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<FieldErrorObject> fields = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(
                        fieldError -> new FieldErrorObject(
                                fieldError.getField(),
                                mensagemValidacao(
                                        fieldError.getField(),
                                        fieldError.getCode(),
                                        fieldError.getDefaultMessage()
                                )
                        )
                )
                .toList();
        String campo = fields.isEmpty()
                ? null
                : fields.getFirst()
                        .field();
        return resposta(
                ErrorEnum.CAMPO_INVALIDO,
                campo,
                fields,
                request
        );
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorObject<Void>> handleBaseException(BaseException exception, HttpServletRequest request) {
        ErrorEnum erro = exception.getErrorEnum();
        log.warn(
                "Erro de negócio. código={}, mensagem={}",
                erro.getErrorCode(),
                exception.getMessage()
        );

        List<FieldErrorObject> fields = exception.getField() == null
                ? null
                : List.of(
                        new FieldErrorObject(
                                exception.getField(),
                                exception.getMessage()
                        )
                );
        return resposta(
                erro,
                null,
                fields,
                request,
                exception.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorObject<Void>> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {
        log.error(
                "Erro inesperado.",
                exception
        );
        return resposta(
                ErrorEnum.ERRO_GENERICO,
                null,
                null,
                request
        );
    }

    private FieldErrorObject paraErroDeCampo(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath()
                .toString();
        int ultimoSeparador = path.lastIndexOf('.');
        String campo = ultimoSeparador >= 0 ? path.substring(ultimoSeparador + 1) : path;
        return new FieldErrorObject(
                campo,
                violation.getMessage()
        );
    }

    private String mensagemValidacao(String campo, String codigo, String mensagemPadrao) {
        return switch (codigo == null ? "" : codigo) {
            case "NotNull", "NotBlank", "NotEmpty" -> "O campo '" + campo + "' é obrigatório.";
            case "Positive" -> "O campo '" + campo + "' deve ser maior que zero.";
            case "PositiveOrZero" -> "O campo '" + campo + "' não pode ser negativo.";
            case "DecimalMin" -> "O campo '" + campo + "' deve respeitar o valor mínimo permitido.";
            case "DecimalMax" -> mensagemPadrao == null
                    ? "O campo '" + campo + "' excede o valor máximo permitido."
                    : mensagemPadrao;
            case "Digits" -> "O campo '" + campo + "' possui precisão ou casas decimais inválidas.";
            case "Pattern" -> mensagemPadrao == null ? "O campo '" + campo + "' possui formato inválido." : mensagemPadrao;
            case "Size" -> "O campo '" + campo + "' possui tamanho inválido.";
            case "Future", "FutureOrPresent" -> "O campo '" + campo + "' não pode estar no passado.";
            case "Past", "PastOrPresent" -> mensagemPadrao == null
                    ? "O campo '" + campo + "' não pode estar no futuro."
                    : mensagemPadrao;
            case "Min", "Max" -> "O campo '" + campo + "' está fora do intervalo permitido.";
            default -> mensagemPadrao == null ? "Valor inválido." : mensagemPadrao;
        };
    }

    private <T> ResponseEntity<ErrorObject<T>> resposta(
            ErrorEnum erro,
            T value,
            List<FieldErrorObject> fields,
            HttpServletRequest request) {
        return resposta(
                erro,
                value,
                fields,
                request,
                erro.getErrorMessage()
        );
    }

    private <T> ResponseEntity<ErrorObject<T>> resposta(
            ErrorEnum erro,
            T value,
            List<FieldErrorObject> fields,
            HttpServletRequest request,
            String mensagem) {
        HttpStatus status = HttpStatus.valueOf(erro.getHttpStatus());
        return ResponseEntity.status(status)
                .body(
                        ErrorObject.<T>builder()
                                .codError(erro.getErrorCode())
                                .msgError(mensagem)
                                .value(value)
                                .timestamp(Instant.now())
                                .status(status.value())
                                .error(status.getReasonPhrase())
                                .code(erro.name())
                                .message(mensagem)
                                .path(request.getRequestURI())
                                .fields(fields == null || fields.isEmpty() ? null : fields)
                                .build()
                );
    }
}
