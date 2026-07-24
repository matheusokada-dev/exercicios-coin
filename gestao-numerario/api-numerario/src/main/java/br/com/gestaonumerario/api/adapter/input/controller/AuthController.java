package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.port.input.AutenticarInputPort;
import br.com.gestaonumerario.api.core.domain.model.command.AutenticarCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AutenticarInputPort autenticarUseCase;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var token = autenticarUseCase.autenticar(new AutenticarCommand(request.login(), request.senha()));
        return new LoginResponse(token.valor(), "Bearer", token.expiraEm());
    }

    public record LoginRequest(@NotBlank String login, @NotBlank String senha) { }
    public record LoginResponse(String accessToken, String tokenType, Instant expiraEm) { }
}



