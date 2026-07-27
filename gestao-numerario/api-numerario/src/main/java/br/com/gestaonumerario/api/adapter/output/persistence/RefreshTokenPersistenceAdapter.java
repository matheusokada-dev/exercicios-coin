package br.com.gestaonumerario.api.adapter.output.persistence;

import br.com.gestaonumerario.api.adapter.output.repository.RefreshTokenJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.UsuarioJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.entity.RefreshTokenEntity;
import br.com.gestaonumerario.api.core.domain.model.RefreshTokenRotacionado;
import br.com.gestaonumerario.api.core.exception.CredenciaisInvalidasException;
import br.com.gestaonumerario.api.port.output.RefreshTokenOutputPort;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenOutputPort {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final RefreshTokenJpaRepository repository;
    private final UsuarioJpaRepository usuarioRepository;
    private final RelogioOutputPort relogioPort;

    @Override
    @Transactional
    public RefreshTokenRotacionado emitir(Long usuarioId, Instant agora, Duration duracao) {
        String valor = novoToken();
        Instant expiraEm = agora.plus(duracao);
        repository.save(new RefreshTokenEntity(
                usuarioRepository.getReferenceById(usuarioId), hash(valor), agora, expiraEm));
        return new RefreshTokenRotacionado(usuarioId, valor, expiraEm);
    }

    @Override
    @Transactional
    public RefreshTokenRotacionado rotacionar(String token, Instant agora, Duration duracao) {
        if (token == null || token.isBlank()) {
            throw new CredenciaisInvalidasException();
        }
        var atual = repository.findByTokenHash(hash(token))
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!atual.validoEm(agora)) {
            throw new CredenciaisInvalidasException();
        }
        atual.revogar(agora);
        return emitir(atual.getUsuario().getId(), agora, duracao);
    }

    @Override
    @Transactional
    public void revogar(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        repository.findByTokenHash(hash(token)).ifPresent(entity -> entity.revogar(relogioPort.agora()));
    }

    @Override
    @Transactional
    public void revogarTodos(Long usuarioId) {
        repository.revogarTodos(usuarioId, relogioPort.agora());
    }

    private String novoToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String valor) {
        try {
            return java.util.HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível.", exception);
        }
    }
}
