package br.com.coin.bffcadastroprodutos.exceptions;

import br.com.coin.bffcadastroprodutos.enums.BffErrorEnum;
import lombok.Getter;

@Getter
public abstract class BffException extends RuntimeException {

    private final BffErrorEnum errorEnum;

    public BffException(BffErrorEnum errorEnum) {
        super(errorEnum.getErrorMessage());
        this.errorEnum = errorEnum;
    }

    public BffException(BffErrorEnum errorEnum, String message) {
        super(message);
        this.errorEnum = errorEnum;
    }
}
