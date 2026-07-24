package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.core.domain.model.command.CriarUsuarioCommand;

public interface UsuarioInputPort {
    Usuario criar(CriarUsuarioCommand command);
    Usuario buscarPorId(Long id);
}


