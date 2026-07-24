package br.com.gestaonumerario.api.core.domain.model;

import java.time.Instant;

public record TokenAcesso(String valor, Instant expiraEm) {
}
