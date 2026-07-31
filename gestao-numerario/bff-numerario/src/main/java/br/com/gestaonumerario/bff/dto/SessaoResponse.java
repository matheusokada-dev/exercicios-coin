package br.com.gestaonumerario.bff.dto;

import java.time.Instant;

public record SessaoResponse(Long usuarioId, String nome, String perfil, Instant expiraEm) {
}
