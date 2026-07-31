package br.com.gestaonumerario.api.adapter.output.repository;

import br.com.gestaonumerario.api.adapter.output.repository.entity.UsuarioEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByLogin(String login);

    boolean existsByLogin(String login);
}
