package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.dto.LoginRequest;
import br.com.gestaonumerario.bff.dto.LoginResponse;
import br.com.gestaonumerario.bff.dto.RefreshRequest;
import br.com.gestaonumerario.bff.dto.SessaoResponse;
import br.com.gestaonumerario.bff.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.autenticar(request);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.renovar(request.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.encerrar(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public SessaoResponse me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return authService.consultarSessao(authorization);
    }
}
