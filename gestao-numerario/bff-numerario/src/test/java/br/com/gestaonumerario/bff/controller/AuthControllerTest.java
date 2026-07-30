package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.dto.LoginRequest;
import br.com.gestaonumerario.bff.dto.LoginResponse;
import br.com.gestaonumerario.bff.dto.SessaoResponse;
import br.com.gestaonumerario.bff.service.AuthService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        login = new LoginResponse(
                "access",
                "Bearer",
                Instant.parse("2026-07-25T12:15:00Z"),
                1L,
                "Gestor",
                "GESTOR"
        );
    }

    @Test
    void autenticaEDevolveTokensNoPayload() {
        var request = new LoginRequest(
                "gestor",
                "senha"
        );
        when(authService.autenticar(request)).thenReturn(login);

        assertThat(controller.login(request)).isEqualTo(login);
        verify(authService).autenticar(request);
    }

    @Test
    void consultaSessaoAtual() {
        var sessao = new SessaoResponse(
                1L,
                "Gestor",
                "GESTOR",
                login.expiraEm()
        );
        when(authService.consultarSessao("Bearer access")).thenReturn(sessao);

        assertThat(controller.me("Bearer access")).isEqualTo(sessao);
    }
}
