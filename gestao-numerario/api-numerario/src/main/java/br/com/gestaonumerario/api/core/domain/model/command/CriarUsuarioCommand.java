package br.com.gestaonumerario.api.core.domain.model.command;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;

public record CriarUsuarioCommand(String nome, String login, String senha, PerfilUsuario perfil) {
}
