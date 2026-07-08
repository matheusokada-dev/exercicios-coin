package br.com.coin.bffcadastroprodutos.handlers;

import br.com.coin.bffcadastroprodutos.dtos.frontend.response.BffErrorDtoResponse;
import br.com.coin.bffcadastroprodutos.enums.BffErrorEnum;
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
    public ResponseEntity<BffErrorDtoResponse> handleException(Exception ex) {
        log.error("Um erro inesperado aconteceu na BFF", ex);

        return ResponseEntity
                .status(BffErrorEnum.ERRO_GENERICO.getHttpStatus())
                .body(new BffErrorDtoResponse(
                        BffErrorEnum.ERRO_GENERICO.getErrorCode(),
                        BffErrorEnum.ERRO_GENERICO.getErrorMessage()
                ));
    }
}
