package br.com.gestaonumerario.api.core.domain.model;

import java.time.Instant;

public record SessaoAutenticacao(
        String accessToken,
        Instant accessTokenExpiraEm,
        String refreshToken,
        Instant refreshTokenExpiraEm,
        Long usuarioId,
        String nome,
        String perfil
) {
}
