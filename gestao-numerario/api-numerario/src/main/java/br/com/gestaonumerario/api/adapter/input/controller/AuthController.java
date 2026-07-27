package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.port.input.AutenticarInputPort;
import br.com.gestaonumerario.api.port.input.UsuarioInputPort;
import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.core.domain.model.command.AutenticarCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AutenticarInputPort autenticarUseCase;
    private final UsuarioInputPort usuarioUseCase;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return LoginResponse.from(autenticarUseCase.autenticar(
                new AutenticarCommand(request.login(), request.senha())));
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return LoginResponse.from(autenticarUseCase.renovar(request.refreshToken()));
    }

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody RefreshRequest request) {
        autenticarUseCase.encerrar(request.refreshToken());
    }

    @org.springframework.web.bind.annotation.GetMapping("/me")
    public SessaoResponse me(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        var usuario = usuarioUseCase.buscarPorId(autenticado.id());
        if (!usuario.isAtivo()) {
            throw new br.com.gestaonumerario.api.core.exception.CredenciaisInvalidasException();
        }
        return new SessaoResponse(usuario.getId(), usuario.getNome(), usuario.getPerfil().name());
    }

    public record LoginRequest(@NotBlank String login, @NotBlank String senha) { }
    public record RefreshRequest(@NotBlank String refreshToken) { }
    public record SessaoResponse(Long usuarioId, String nome, String perfil) { }
    public record LoginResponse(
            String accessToken, String tokenType, Instant expiraEm,
            String refreshToken, Instant refreshExpiraEm,
            Long usuarioId, String nome, String perfil) {
        static LoginResponse from(br.com.gestaonumerario.api.core.domain.model.SessaoAutenticacao sessao) {
            return new LoginResponse(
                    sessao.accessToken(), "Bearer", sessao.accessTokenExpiraEm(),
                    sessao.refreshToken(), sessao.refreshTokenExpiraEm(),
                    sessao.usuarioId(), sessao.nome(), sessao.perfil());
        }
    }
}
