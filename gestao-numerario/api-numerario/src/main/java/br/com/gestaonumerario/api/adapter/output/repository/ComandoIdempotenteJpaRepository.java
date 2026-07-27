package br.com.gestaonumerario.api.adapter.output.repository;

import br.com.gestaonumerario.api.adapter.output.repository.entity.ComandoIdempotenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComandoIdempotenteJpaRepository extends JpaRepository<ComandoIdempotenteEntity,Long> {
    boolean existsByIdempotencyKey(String key);
    boolean existsByTipoComando(String tipoComando);
}
