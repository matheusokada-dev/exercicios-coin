package br.com.gestaonumerario.api.core.exception;

public class LoginDuplicadoException extends BaseException {

    public LoginDuplicadoException() {
        super(ErrorEnum.LOGIN_JA_CADASTRADO);
    }
}
