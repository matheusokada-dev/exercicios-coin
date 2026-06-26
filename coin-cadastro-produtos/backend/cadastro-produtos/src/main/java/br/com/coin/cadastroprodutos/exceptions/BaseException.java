package br.com.coin.cadastroprodutos.exceptions;

import br.com.coin.cadastroprodutos.enums.ErrorEnum;
import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    private final ErrorEnum errorEnum;

    public BaseException(ErrorEnum errorEnum) {
        super(errorEnum.getErrorMessage());

        this.errorEnum = errorEnum;
    }
}
