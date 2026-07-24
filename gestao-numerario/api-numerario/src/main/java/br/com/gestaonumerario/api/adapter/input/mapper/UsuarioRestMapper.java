package br.com.gestaonumerario.api.adapter.input.mapper;

import br.com.gestaonumerario.api.adapter.input.controller.dto.request.CriarUsuarioRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.UsuarioResponse;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.core.domain.model.command.CriarUsuarioCommand;
import org.springframework.stereotype.Component;

@Component
public class UsuarioRestMapper {

    public CriarUsuarioCommand toCommand(CriarUsuarioRequest request) {
        return new CriarUsuarioCommand(request.nome(), request.login(), request.senha(), request.perfil());
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getLogin(), usuario.getPerfil(),
                usuario.isAtivo(), usuario.getCriadoEm());
    }
}


