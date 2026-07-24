package br.com.gestaonumerario.api.core.exception;

public class UsuarioNaoEncontradoException extends BaseException {

    public UsuarioNaoEncontradoException() {
        super(ErrorEnum.USUARIO_NAO_ENCONTRADO);
    }
}
