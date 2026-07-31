package br.com.gestaonumerario.api.adapter.output.persistence;

import br.com.gestaonumerario.api.adapter.output.mapper.UsuarioPersistenceMapper;
import br.com.gestaonumerario.api.adapter.output.repository.UsuarioJpaRepository;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.port.output.UsuarioOutputPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioPersistenceAdapter implements UsuarioOutputPort {

    private final UsuarioJpaRepository repository;
    private final UsuarioPersistenceMapper mapper;

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return repository.findByLogin(login)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existePorLogin(String login) {
        return repository.existsByLogin(login);
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return mapper.toDomain(repository.save(mapper.toEntity(usuario)));
    }
}
