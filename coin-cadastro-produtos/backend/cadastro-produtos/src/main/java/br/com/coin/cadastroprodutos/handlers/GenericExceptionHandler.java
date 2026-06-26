package br.com.coin.cadastroprodutos.handlers;

import br.com.coin.cadastroprodutos.enums.ErrorEnum;
import br.com.coin.cadastroprodutos.exceptions.ErrorObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GenericExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorObject<Object>> handleException(Exception e) {
        log.error("Um erro aconteceu: {}", e.getMessage());

        log.error(e.toString());
        final ErrorObject<Object> errorObject = ErrorObject
                .builder()
                .codError(ErrorEnum.ERRO_GENERICO.getErrorCode())
                .msgError(ErrorEnum.ERRO_GENERICO.getErrorMessage())
                .build();

        return ResponseEntity
                .internalServerError()
                .body(errorObject);
    }
}
