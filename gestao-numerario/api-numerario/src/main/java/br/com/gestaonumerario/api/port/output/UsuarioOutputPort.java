package br.com.gestaonumerario.api.port.output;

import br.com.gestaonumerario.api.core.domain.model.Usuario;

import java.util.Optional;

public interface UsuarioOutputPort {

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorLogin(String login);

    boolean existePorLogin(String login);

    Usuario salvar(Usuario usuario);
}

