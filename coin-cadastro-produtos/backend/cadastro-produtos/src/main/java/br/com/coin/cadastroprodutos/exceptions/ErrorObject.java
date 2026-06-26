package br.com.coin.cadastroprodutos.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorObject<T> {
    private int codError;
    private String msgError;
    private T value;
}
