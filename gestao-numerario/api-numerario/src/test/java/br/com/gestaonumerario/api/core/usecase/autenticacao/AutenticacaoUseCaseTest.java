package br.com.gestaonumerario.api.core.usecase.autenticacao;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.model.TokenAcesso;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.core.domain.model.command.AutenticarCommand;
import br.com.gestaonumerario.api.core.exception.CredenciaisInvalidasException;
import br.com.gestaonumerario.api.port.output.CodificadorSenhaOutputPort;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import br.com.gestaonumerario.api.port.output.TokenJwtOutputPort;
import br.com.gestaonumerario.api.port.output.UsuarioOutputPort;
import br.com.gestaonumerario.api.port.output.RefreshTokenOutputPort;
import br.com.gestaonumerario.api.core.domain.model.RefreshTokenRotacionado;
import br.com.gestaonumerario.api.core.domain.model.SessaoAutenticacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutenticacaoUseCaseTest {

    private static final Instant AGORA = Instant.parse("2026-07-24T12:00:00Z");
    private static final Duration DURACAO_BLOQUEIO = Duration.ofMinutes(15);

    private UsuarioOutputPort usuarioPort;
    private CodificadorSenhaOutputPort senhaPort;
    private TokenJwtOutputPort tokenPort;
    private RelogioOutputPort relogioPort;
    private AutenticacaoUseCase useCase;
    private RefreshTokenOutputPort refreshTokenPort;
    private Usuario usuario;

    @BeforeEach
    void configurar() {
        usuarioPort = mock(UsuarioOutputPort.class);
        senhaPort = mock(CodificadorSenhaOutputPort.class);
        tokenPort = mock(TokenJwtOutputPort.class);
        relogioPort = mock(RelogioOutputPort.class);
        refreshTokenPort = mock(RefreshTokenOutputPort.class);
        usuario = new Usuario(1L, "Gestor", "gestor", "hash", PerfilUsuario.GESTOR,
                true, AGORA, 0, null);

        when(usuarioPort.buscarPorLogin("gestor")).thenReturn(Optional.of(usuario));
        when(usuarioPort.salvar(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        when(relogioPort.agora()).thenReturn(AGORA);
        when(tokenPort.gerar(usuario)).thenReturn(new TokenAcesso("token", AGORA.plusSeconds(3600)));
        when(refreshTokenPort.emitir(1L, AGORA, Duration.ofHours(8)))
                .thenReturn(new RefreshTokenRotacionado(1L, "refresh", AGORA.plus(Duration.ofHours(8))));

        useCase = new AutenticacaoUseCase(
                usuarioPort, senhaPort, tokenPort, relogioPort, 5, DURACAO_BLOQUEIO,
                Duration.ofHours(8), refreshTokenPort);
    }

    @Test
    void deveBloquearPorQuinzeMinutosAposCincoSenhasInvalidas() {
        when(senhaPort.confere("errada", "hash")).thenReturn(false);

        for (int tentativa = 0; tentativa < 4; tentativa++) {
            assertThrows(CredenciaisInvalidasException.class,
                    () -> useCase.autenticar(new AutenticarCommand("gestor", "errada")));
        }

        CredenciaisInvalidasException quintaFalha = assertThrows(
                CredenciaisInvalidasException.class,
                () -> useCase.autenticar(new AutenticarCommand("gestor", "errada"))
        );

        assertEquals(5, usuario.getTentativasLoginFalhas());
        assertEquals(AGORA.plus(DURACAO_BLOQUEIO), usuario.getBloqueadoAte());
        assertEquals(0, quintaFalha.getTentativasRestantes());
        assertEquals(AGORA.plus(DURACAO_BLOQUEIO), quintaFalha.getBloqueadoAte());
        assertTrue(usuario.estaBloqueado(AGORA.plusSeconds(1)));
    }

    @Test
    void deveLimparFalhasDepoisDeLoginValido() {
        usuario.registrarFalhaLogin(AGORA, 5, DURACAO_BLOQUEIO);
        when(senhaPort.confere("correta", "hash")).thenReturn(true);

        SessaoAutenticacao token = useCase.autenticar(new AutenticarCommand("gestor", "correta"));

        assertEquals("token", token.accessToken());
        assertEquals("refresh", token.refreshToken());
        assertEquals(0, usuario.getTentativasLoginFalhas());
        assertFalse(usuario.possuiFalhasLogin());
        verify(usuarioPort).salvar(usuario);
    }

    @Test
    void naoDeveCompararSenhaEnquantoUsuarioEstiverBloqueado() {
        for (int tentativa = 0; tentativa < 5; tentativa++) {
            usuario.registrarFalhaLogin(AGORA, 5, DURACAO_BLOQUEIO);
        }

        CredenciaisInvalidasException falha = assertThrows(
                CredenciaisInvalidasException.class,
                () -> useCase.autenticar(new AutenticarCommand("gestor", "correta"))
        );

        assertEquals(0, falha.getTentativasRestantes());
        assertEquals(AGORA.plus(DURACAO_BLOQUEIO), falha.getBloqueadoAte());
        verify(senhaPort, never()).confere(any(), any());
        verify(tokenPort, never()).gerar(any());
    }

    @Test
    void deveRotacionarRefreshTokenEEmitirNovoAccessToken() {
        when(refreshTokenPort.rotacionar("refresh-anterior", AGORA, Duration.ofHours(8)))
                .thenReturn(new RefreshTokenRotacionado(
                        1L, "refresh-novo", AGORA.plus(Duration.ofHours(8))));
        when(usuarioPort.buscarPorId(1L)).thenReturn(Optional.of(usuario));

        SessaoAutenticacao sessao = useCase.renovar("refresh-anterior");

        assertEquals("token", sessao.accessToken());
        assertEquals("refresh-novo", sessao.refreshToken());
        verify(refreshTokenPort).rotacionar(
                "refresh-anterior", AGORA, Duration.ofHours(8));
    }

    @Test
    void deveRevogarSessoesQuandoUsuarioForBloqueado() {
        when(senhaPort.confere("errada", "hash")).thenReturn(false);

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            assertThrows(CredenciaisInvalidasException.class,
                    () -> useCase.autenticar(new AutenticarCommand("gestor", "errada")));
        }

        verify(refreshTokenPort).revogarTodos(1L);
    }
}
