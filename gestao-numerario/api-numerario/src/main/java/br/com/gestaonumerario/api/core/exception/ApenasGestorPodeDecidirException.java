package br.com.gestaonumerario.api.core.exception;

public class ApenasGestorPodeDecidirException extends BaseException {

    public ApenasGestorPodeDecidirException() {
        super(ErrorEnum.APENAS_GESTOR_PODE_DECIDIR);
    }
}