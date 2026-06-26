package br.com.coin.cadastroprodutos.handlers;

import br.com.coin.cadastroprodutos.enums.ErrorEnum;
import br.com.coin.cadastroprodutos.exceptions.BaseException;
import br.com.coin.cadastroprodutos.exceptions.ErrorObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BaseExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorObject<?>> handleBaseException(BaseException e) {
        log.error("Um erro aconteceu: {}", e.getMessage());

        final ErrorEnum errorEnum = e.getErrorEnum();

        final ErrorObject<?> errorObject = ErrorObject
                .builder()
                .codError(errorEnum.getErrorCode())
                .msgError(errorEnum.getErrorMessage())
                .build();

        return ResponseEntity
                .status(errorEnum.getHttpStatus())
                .body(errorObject);
    }
}
