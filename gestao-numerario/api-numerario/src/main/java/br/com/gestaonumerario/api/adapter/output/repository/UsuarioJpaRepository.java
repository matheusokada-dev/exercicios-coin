package br.com.gestaonumerario.api.adapter.output.repository;

import br.com.gestaonumerario.api.adapter.output.repository.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByLogin(String login);

    boolean existsByLogin(String login);
}

