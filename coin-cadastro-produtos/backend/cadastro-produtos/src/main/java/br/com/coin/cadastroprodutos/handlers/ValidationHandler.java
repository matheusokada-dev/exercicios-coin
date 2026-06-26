package br.com.coin.cadastroprodutos.handlers;

import br.com.coin.cadastroprodutos.exceptions.ErrorObject;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidationHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorObject<Void>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Request inválido");

        ErrorObject<Void> erro = ErrorObject.<Void>builder()
                .codError(HttpStatus.BAD_REQUEST.value())
                .msgError(mensagem)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(erro);
    }
}