package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 80) String login,
        @NotBlank @Size(max = 72) String senha,
        @NotNull PerfilUsuario perfil
) {
}
