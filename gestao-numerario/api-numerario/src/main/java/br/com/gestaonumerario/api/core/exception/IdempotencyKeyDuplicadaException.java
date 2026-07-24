package br.com.gestaonumerario.api.core.exception;

public class IdempotencyKeyDuplicadaException extends BaseException {

    public IdempotencyKeyDuplicadaException() {
        super(ErrorEnum.IDEMPOTENCY_KEY_DUPLICADA);
    }
}
