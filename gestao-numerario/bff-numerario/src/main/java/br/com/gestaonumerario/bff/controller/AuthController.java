package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.contract.AuthApi;
import br.com.gestaonumerario.bff.dto.LoginRequest;
import br.com.gestaonumerario.bff.dto.LoginResponse;
import br.com.gestaonumerario.bff.dto.SessaoResponse;
import br.com.gestaonumerario.bff.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {
    private final AuthService authService;

    @PostMapping("/login")
    @Override
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.autenticar(request);
    }

    @GetMapping("/me")
    @Override
    public SessaoResponse me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return authService.consultarSessao(authorization);
    }
}
