package br.com.gestaonumerario.api.adapter.input.auth;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;

public record UsuarioAutenticado(Long id, String login, PerfilUsuario perfil) {
}
