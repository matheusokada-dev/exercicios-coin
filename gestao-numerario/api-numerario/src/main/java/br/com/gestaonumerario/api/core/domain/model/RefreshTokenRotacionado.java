package br.com.gestaonumerario.api.core.domain.model;

import java.time.Instant;

public record RefreshTokenRotacionado(Long usuarioId, String valor, Instant expiraEm) {
}
