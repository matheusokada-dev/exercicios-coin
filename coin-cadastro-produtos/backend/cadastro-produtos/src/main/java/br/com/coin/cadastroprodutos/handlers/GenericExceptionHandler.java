package br.com.coin.cadastroprodutos.handlers;

import br.com.coin.cadastroprodutos.enums.ErrorEnum;
import br.com.coin.cadastroprodutos.exceptions.ErrorObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GenericExceptionHandler {
    @ExceptionHandler({DataAccessException.class, CannotCreateTransactionException.class})
    public ResponseEntity<ErrorObject<Object>> handleDatabaseException(Exception e) {
        log.error("Falha de conexão", e);

        final ErrorObject<Object> errorObject = ErrorObject
                .builder()
                .codError(ErrorEnum.BANCO_INDISPONIVEL.getErrorCode())
                .msgError(ErrorEnum.BANCO_INDISPONIVEL.getErrorMessage())
                .build();

        return ResponseEntity
                .status(ErrorEnum.BANCO_INDISPONIVEL.getHttpStatus())
                .body(errorObject);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorObject<Object>> handleException(Exception e) {
        log.error("Um erro inesperado aconteceu", e);
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
