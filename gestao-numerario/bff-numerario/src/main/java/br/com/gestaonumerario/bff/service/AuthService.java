package br.com.gestaonumerario.bff.service;

import br.com.gestaonumerario.bff.client.ApiNumerarioClient;
import br.com.gestaonumerario.bff.dto.LoginRequest;
import br.com.gestaonumerario.bff.dto.LoginResponse;
import br.com.gestaonumerario.bff.dto.SessaoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ApiNumerarioClient apiNumerarioClient;

    public LoginResponse autenticar(LoginRequest request) {
        return apiNumerarioClient.autenticar(request);
    }

    public LoginResponse renovar(String refreshToken) {
        return apiNumerarioClient.renovar(refreshToken);
    }

    public void encerrar(String refreshToken) {
        apiNumerarioClient.encerrar(refreshToken);
    }

    public SessaoResponse consultarSessao(String authorization) {
        return apiNumerarioClient.consultarSessao(authorization);
    }
}
