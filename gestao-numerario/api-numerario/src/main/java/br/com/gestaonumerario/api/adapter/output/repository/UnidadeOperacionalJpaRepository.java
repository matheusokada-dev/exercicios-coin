package br.com.gestaonumerario.api.adapter.output.repository;

import br.com.gestaonumerario.api.adapter.output.repository.entity.UnidadeOperacionalEntity;
import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UnidadeOperacionalJpaRepository extends JpaRepository<UnidadeOperacionalEntity, Long> {
    List<UnidadeOperacionalEntity> findByAtivoTrueOrderByNomeAsc();
    List<UnidadeOperacionalEntity> findByTipoAndAtivoTrueOrderByNomeAsc(TipoUnidadeOperacional tipo);
    Optional<UnidadeOperacionalEntity> findByCodigo(String codigo);
    Optional<UnidadeOperacionalEntity> findByAgencia_Id(Long agenciaId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UnidadeOperacionalEntity u where u.id=:id")
    Optional<UnidadeOperacionalEntity> buscarParaAtualizacao(@Param("id") Long id);
}
