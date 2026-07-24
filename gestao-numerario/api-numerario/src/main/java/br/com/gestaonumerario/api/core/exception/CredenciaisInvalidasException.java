package br.com.gestaonumerario.api.core.exception;

import java.time.Instant;

public class CredenciaisInvalidasException extends BaseException {

    private final Integer tentativasRestantes;
    private final Instant bloqueadoAte;

    public CredenciaisInvalidasException() {
        this(null, null);
    }

    public CredenciaisInvalidasException(Integer tentativasRestantes, Instant bloqueadoAte) {
        super(ErrorEnum.CREDENCIAIS_INVALIDAS);
        this.tentativasRestantes = tentativasRestantes;
        this.bloqueadoAte = bloqueadoAte;
    }

    public Integer getTentativasRestantes() {
        return tentativasRestantes;
    }

    public Instant getBloqueadoAte() {
        return bloqueadoAte;
    }
}
