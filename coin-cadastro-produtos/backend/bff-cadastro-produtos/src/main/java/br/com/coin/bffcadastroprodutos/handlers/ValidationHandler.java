package br.com.coin.bffcadastroprodutos.handlers;

import br.com.coin.bffcadastroprodutos.dtos.frontend.BffErrorDtoResponse;
import br.com.coin.bffcadastroprodutos.enums.BffErrorEnum;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidationHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BffErrorDtoResponse> handleValidation(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse(BffErrorEnum.REQUISICAO_INVALIDA.getErrorMessage());

        return ResponseEntity
                .status(BffErrorEnum.REQUISICAO_INVALIDA.getHttpStatus())
                .body(new BffErrorDtoResponse(BffErrorEnum.REQUISICAO_INVALIDA.getErrorCode(), mensagem));
    }
}
