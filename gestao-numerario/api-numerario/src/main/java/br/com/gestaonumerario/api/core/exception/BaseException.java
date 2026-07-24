package br.com.gestaonumerario.api.core.exception;

public abstract class BaseException extends RuntimeException {

    private final ErrorEnum errorEnum;

    protected BaseException(ErrorEnum errorEnum) {
        super(errorEnum.getErrorMessage());
        this.errorEnum = errorEnum;
    }

    public ErrorEnum getErrorEnum() {
        return errorEnum;
    }
}
