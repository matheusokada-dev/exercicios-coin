package br.com.gestaonumerario.api.adapter.input.controller.dto.response;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;

import java.time.Instant;

public record UsuarioResponse(
        Long id, String nome, String login, PerfilUsuario perfil, boolean ativo, Instant criadoEm
) {
}

