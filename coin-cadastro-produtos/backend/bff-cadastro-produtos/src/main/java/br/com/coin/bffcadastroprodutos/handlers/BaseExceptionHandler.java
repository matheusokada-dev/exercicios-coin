package br.com.coin.bffcadastroprodutos.handlers;

import br.com.coin.bffcadastroprodutos.dtos.frontend.BffErrorDtoResponse;
import br.com.coin.bffcadastroprodutos.enums.BffErrorEnum;
import br.com.coin.bffcadastroprodutos.exceptions.BackendResponseException;
import br.com.coin.bffcadastroprodutos.exceptions.BffException;
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

    @ExceptionHandler(BffException.class)
    public ResponseEntity<BffErrorDtoResponse> handleBffException(BffException ex) {
        log.error("Um erro controlado aconteceu na BFF: {}", ex.getMessage());

        final BffErrorEnum errorEnum = ex.getErrorEnum();
        final BffErrorDtoResponse error = new BffErrorDtoResponse(
                errorEnum.getErrorCode(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(errorEnum.getHttpStatus())
                .body(error);
    }

    @ExceptionHandler(BackendResponseException.class)
    public ResponseEntity<BffErrorDtoResponse> handleBackendResponseException(BackendResponseException ex) {
        log.error("Backend de produtos respondeu com erro: {}", ex.getMessage());

        return ResponseEntity
                .status(ex.getStatus())
                .body(new BffErrorDtoResponse(ex.getCodError(), ex.getMessage()));
    }
}
