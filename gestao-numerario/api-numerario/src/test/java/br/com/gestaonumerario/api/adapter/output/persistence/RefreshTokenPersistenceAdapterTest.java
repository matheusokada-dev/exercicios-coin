package br.com.gestaonumerario.api.adapter.output.persistence;

import br.com.gestaonumerario.api.adapter.output.repository.RefreshTokenJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.UsuarioJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.entity.RefreshTokenEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.UsuarioEntity;
import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.exception.CredenciaisInvalidasException;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RefreshTokenPersistenceAdapterTest {

    private static final Instant AGORA = Instant.parse("2026-07-25T12:00:00Z");
    private RefreshTokenJpaRepository repository;
    private UsuarioJpaRepository usuarioRepository;
    private RelogioOutputPort relogio;
    private RefreshTokenPersistenceAdapter adapter;
    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
        repository = mock(RefreshTokenJpaRepository.class);
        usuarioRepository = mock(UsuarioJpaRepository.class);
        relogio = mock(RelogioOutputPort.class);
        adapter = new RefreshTokenPersistenceAdapter(repository, usuarioRepository, relogio);
        usuario = new UsuarioEntity(7L, "Gestor", "gestor", "hash",
                PerfilUsuario.GESTOR, true, AGORA, 0, null);
        when(usuarioRepository.getReferenceById(7L)).thenReturn(usuario);
    }

    @Test
    void emiteTokenAleatorioEArmazenaSomenteHash() {
        var emitido = adapter.emitir(7L, AGORA, Duration.ofHours(8));

        assertThat(emitido.usuarioId()).isEqualTo(7L);
        assertThat(emitido.valor()).hasSize(43);
        assertThat(emitido.expiraEm()).isEqualTo(AGORA.plus(Duration.ofHours(8)));
        verify(repository).save(argThat(entity ->
                entity.getUsuario() == usuario
                        && !entity.getTokenHash().equals(emitido.valor())
                        && entity.getTokenHash().length() == 64));
    }

    @Test
    void rotacionaTokenValidoRevogandoAnterior() {
        var anterior = new RefreshTokenEntity(usuario, "hash", AGORA.minusSeconds(10), AGORA.plusSeconds(60));
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(anterior));

        var novo = adapter.rotacionar("token-anterior", AGORA, Duration.ofHours(1));

        assertThat(anterior.getRevogadoEm()).isEqualTo(AGORA);
        assertThat(novo.usuarioId()).isEqualTo(7L);
        verify(repository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void rejeitaRotacaoSemTokenInexistenteExpiradoOuRevogado() {
        assertThatThrownBy(() -> adapter.rotacionar(" ", AGORA, Duration.ofHours(1)))
                .isInstanceOf(CredenciaisInvalidasException.class);

        when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adapter.rotacionar("inexistente", AGORA, Duration.ofHours(1)))
                .isInstanceOf(CredenciaisInvalidasException.class);

        var expirado = new RefreshTokenEntity(usuario, "hash", AGORA.minusSeconds(20), AGORA.minusSeconds(1));
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(expirado));
        assertThatThrownBy(() -> adapter.rotacionar("expirado", AGORA, Duration.ofHours(1)))
                .isInstanceOf(CredenciaisInvalidasException.class);

        var revogado = new RefreshTokenEntity(usuario, "hash", AGORA.minusSeconds(20), AGORA.plusSeconds(60));
        revogado.revogar(AGORA.minusSeconds(1));
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(revogado));
        assertThatThrownBy(() -> adapter.rotacionar("revogado", AGORA, Duration.ofHours(1)))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void revogaTokenIndividualETodosDoUsuario() {
        var token = new RefreshTokenEntity(usuario, "hash", AGORA.minusSeconds(10), AGORA.plusSeconds(60));
        when(relogio.agora()).thenReturn(AGORA);
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        adapter.revogar("token");
        adapter.revogarTodos(7L);

        assertThat(token.getRevogadoEm()).isEqualTo(AGORA);
        verify(repository).revogarTodos(7L, AGORA);
    }

    @Test
    void revogarSemTokenNaoConsultaRepositorio() {
        adapter.revogar(null);
        adapter.revogar(" ");

        verifyNoInteractions(repository, relogio);
    }
}
