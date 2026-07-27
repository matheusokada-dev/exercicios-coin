package br.com.gestaonumerario.api.port.output;

import br.com.gestaonumerario.api.core.domain.model.RefreshTokenRotacionado;

import java.time.Duration;
import java.time.Instant;

public interface RefreshTokenOutputPort {
    RefreshTokenRotacionado emitir(Long usuarioId, Instant agora, Duration duracao);
    RefreshTokenRotacionado rotacionar(String token, Instant agora, Duration duracao);
    void revogar(String token);
    void revogarTodos(Long usuarioId);
}
