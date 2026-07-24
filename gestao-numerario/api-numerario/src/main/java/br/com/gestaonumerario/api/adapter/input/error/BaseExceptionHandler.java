package br.com.gestaonumerario.api.adapter.input.error;

import br.com.gestaonumerario.api.core.exception.BaseException;
import br.com.gestaonumerario.api.core.exception.CredenciaisInvalidasException;
import br.com.gestaonumerario.api.core.exception.ErrorEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BaseExceptionHandler {

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErrorObject<DetalheFalhaAutenticacao>> handleCredenciaisInvalidas(
            CredenciaisInvalidasException exception
    ) {
        ErrorEnum error = exception.getErrorEnum();
        DetalheFalhaAutenticacao detalhe = exception.getTentativasRestantes() == null
                ? null
                : new DetalheFalhaAutenticacao(
                        exception.getTentativasRestantes(),
                        exception.getBloqueadoAte()
                );

        return ResponseEntity
                .status(error.getHttpStatus())
                .body(ErrorObject.<DetalheFalhaAutenticacao>builder()
                        .codError(error.getErrorCode())
                        .msgError(error.getErrorMessage())
                        .value(detalhe)
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorObject<String>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ) {
        return ResponseEntity
                .status(ErrorEnum.CAMPO_OBRIGATORIO.getHttpStatus())
                .body(ErrorObject.<String>builder()
                        .codError(ErrorEnum.CAMPO_OBRIGATORIO.getErrorCode())
                        .msgError(ErrorEnum.CAMPO_OBRIGATORIO.getErrorMessage())
                        .value(exception.getName())
                        .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorObject<Void>> handleConstraintViolationException(
            ConstraintViolationException exception
    ) {
        return ResponseEntity
                .status(ErrorEnum.CAMPO_OBRIGATORIO.getHttpStatus())
                .body(ErrorObject.<Void>builder()
                        .codError(ErrorEnum.CAMPO_OBRIGATORIO.getErrorCode())
                        .msgError(ErrorEnum.CAMPO_OBRIGATORIO.getErrorMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorObject<String>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        String campo = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField())
                .orElse(null);

        ErrorObject<String> errorObject = ErrorObject.<String>builder()
                .codError(ErrorEnum.CAMPO_OBRIGATORIO.getErrorCode())
                .msgError(ErrorEnum.CAMPO_OBRIGATORIO.getErrorMessage())
                .value(campo)
                .build();

        return ResponseEntity
                .status(ErrorEnum.CAMPO_OBRIGATORIO.getHttpStatus())
                .body(errorObject);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorObject<Void>> handleBaseException(BaseException exception) {
        ErrorEnum errorEnum = exception.getErrorEnum();

        log.warn(
                "Erro de negÃ³cio. cÃ³digo={}, mensagem={}",
                errorEnum.getErrorCode(),
                errorEnum.getErrorMessage()
        );

        ErrorObject<Void> errorObject = ErrorObject.<Void>builder()
                .codError(errorEnum.getErrorCode())
                .msgError(errorEnum.getErrorMessage())
                .build();

        return ResponseEntity
                .status(errorEnum.getHttpStatus())
                .body(errorObject);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorObject<Void>> handleUnexpectedException(Exception exception) {
        log.error("Erro inesperado.", exception);

        ErrorObject<Void> errorObject = ErrorObject.<Void>builder()
                .codError(ErrorEnum.ERRO_GENERICO.getErrorCode())
                .msgError(ErrorEnum.ERRO_GENERICO.getErrorMessage())
                .build();

        return ResponseEntity
                .status(ErrorEnum.ERRO_GENERICO.getHttpStatus())
                .body(errorObject);
    }
}

