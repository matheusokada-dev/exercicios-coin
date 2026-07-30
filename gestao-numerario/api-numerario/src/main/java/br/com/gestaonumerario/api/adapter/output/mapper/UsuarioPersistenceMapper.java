package br.com.gestaonumerario.api.adapter.output.mapper;

import br.com.gestaonumerario.api.adapter.output.repository.entity.UsuarioEntity;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioPersistenceMapper {

    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Usuario(
                entity.getId(),
                entity.getNome(),
                entity.getLogin(),
                entity.getSenhaHash(),
                entity.getPerfil(),
                entity.isAtivo(),
                entity.getCriadoEm(),
                entity.getTentativasLoginFalhas(),
                entity.getBloqueadoAte()
        );
    }

    public UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) {
            return null;
        }

        return new UsuarioEntity(
                domain.getId(),
                domain.getNome(),
                domain.getLogin(),
                domain.getSenhaHash(),
                domain.getPerfil(),
                domain.isAtivo(),
                domain.getCriadoEm(),
                domain.getTentativasLoginFalhas(),
                domain.getBloqueadoAte()
        );
    }
}
