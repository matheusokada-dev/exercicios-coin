package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.dto.LoginRequest;
import br.com.gestaonumerario.bff.dto.LoginResponse;
import br.com.gestaonumerario.bff.dto.RefreshRequest;
import br.com.gestaonumerario.bff.dto.SessaoResponse;
import br.com.gestaonumerario.bff.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private AuthService authService;
    private AuthController controller;
    private LoginResponse login;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        controller = new AuthController(authService);
        login = new LoginResponse("access", "Bearer", Instant.parse("2026-07-25T12:15:00Z"),
                "refresh", Instant.parse("2026-07-25T20:00:00Z"), 1L, "Gestor", "GESTOR");
    }

    @Test
    void autenticaEDevolveTokensNoPayload() {
        var request = new LoginRequest("gestor", "senha");
        when(authService.autenticar(request)).thenReturn(login);

        assertThat(controller.login(request)).isEqualTo(login);
        verify(authService).autenticar(request);
    }

    @Test
    void renovaUsandoRefreshTokenDoPayload() {
        var request = new RefreshRequest("refresh");
        when(authService.renovar("refresh")).thenReturn(login);

        assertThat(controller.refresh(request)).isEqualTo(login);
        verify(authService).renovar("refresh");
    }

    @Test
    void encerraSessaoUsandoRefreshTokenDoPayload() {
        var response = controller.logout(new RefreshRequest("refresh"));

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(authService).encerrar("refresh");
    }

    @Test
    void consultaSessaoAtual() {
        var sessao = new SessaoResponse(1L, "Gestor", "GESTOR", login.expiraEm());
        when(authService.consultarSessao("Bearer access")).thenReturn(sessao);

        assertThat(controller.me("Bearer access")).isEqualTo(sessao);
    }
}
