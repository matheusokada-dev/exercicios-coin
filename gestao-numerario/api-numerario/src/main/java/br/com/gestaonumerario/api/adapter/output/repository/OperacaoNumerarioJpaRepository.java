package br.com.gestaonumerario.api.adapter.output.repository;

import br.com.gestaonumerario.api.adapter.output.repository.entity.OperacaoNumerarioEntity;
import br.com.gestaonumerario.api.core.domain.enums.StatusOperacaoNumerario;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Optional;

public interface OperacaoNumerarioJpaRepository extends JpaRepository<OperacaoNumerarioEntity, Long> {
    boolean existsByIdempotencyKey(String key);
    @EntityGraph(attributePaths={"solicitacao","solicitacao.agencia","solicitacao.origem",
            "solicitacao.destino","solicitacao.solicitante","solicitacao.decisor",
            "origem","destino","programadoPor","expedidoPor","recebidoPor","conciliadoPor"})
    Optional<OperacaoNumerarioEntity> findBySolicitacao_Id(Long solicitacaoId);
    @EntityGraph(attributePaths={"solicitacao","origem","destino","programadoPor"})
    @Query("""
        select o from OperacaoNumerarioEntity o
        where (:status is null or o.status=:status)
          and (:origemId is null or o.origem.id=:origemId)
          and (:destinoId is null or o.destino.id=:destinoId)
          and (:inicio is null or o.dataProgramacao>=:inicio)
          and (:fim is null or o.dataProgramacao<:fim)
        """)
    Page<OperacaoNumerarioEntity> buscar(@Param("status") StatusOperacaoNumerario status,
            @Param("origemId") Long origemId, @Param("destinoId") Long destinoId,
            @Param("inicio") Instant inicio, @Param("fim") Instant fim, Pageable pageable);
}
