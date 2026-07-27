package br.com.gestaonumerario.bff.dto;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiraEm,
        String refreshToken,
        Instant refreshExpiraEm,
        Long usuarioId,
        String nome,
        String perfil) {
}
