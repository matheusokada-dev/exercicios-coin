package br.com.gestaonumerario.api.core.exception;

public class SaldoInsuficienteException extends BaseException {

    public SaldoInsuficienteException() {
        super(ErrorEnum.SALDO_INSUFICIENTE);
    }
}