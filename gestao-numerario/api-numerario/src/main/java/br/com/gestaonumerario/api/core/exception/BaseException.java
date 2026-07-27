package br.com.gestaonumerario.api.core.exception;

public abstract class BaseException extends RuntimeException {

    private final ErrorEnum errorEnum;
    private final String field;

    protected BaseException(ErrorEnum errorEnum) {
        this(errorEnum, errorEnum.getErrorMessage(), null);
    }

    protected BaseException(ErrorEnum errorEnum, String message) {
        this(errorEnum, message, null);
    }

    protected BaseException(ErrorEnum errorEnum, String message, String field) {
        super(message);
        this.errorEnum = errorEnum;
        this.field = field;
    }

    public ErrorEnum getErrorEnum() {
        return errorEnum;
    }

    public String getField() {
        return field;
    }
}
