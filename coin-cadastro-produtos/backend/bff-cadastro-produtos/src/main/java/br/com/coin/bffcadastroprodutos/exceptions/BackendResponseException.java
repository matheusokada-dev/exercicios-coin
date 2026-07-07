package br.com.coin.bffcadastroprodutos.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BackendResponseException extends RuntimeException {

    private final HttpStatus status;
    private final Integer codError;

    public BackendResponseException(HttpStatus status, Integer codError, String message) {
        super(message);
        this.status = status;
        this.codError = codError;
    }
}
