package br.com.gestaonumerario.api.adapter.output.repository;

import br.com.gestaonumerario.api.adapter.output.repository.entity.SolicitacaoAbastecimentoEntity;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Collection;
import java.util.Optional;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolicitacaoAbastecimentoJpaRepository
        extends JpaRepository<SolicitacaoAbastecimentoEntity, Long> {

    boolean existsByAgencia_IdAndStatusIn(
            Long agenciaId,
            Collection<StatusSolicitacao> status
    );

    long countByStatus(StatusSolicitacao status);

    @EntityGraph(attributePaths = {"agencia", "solicitante", "decisor"})
    Optional<SolicitacaoAbastecimentoEntity> findByAgencia_IdAndStatus(
            Long agenciaId,
            StatusSolicitacao status
    );

    @EntityGraph(attributePaths = {"agencia", "solicitante", "decisor"})
    @Query("""
            select solicitacao from SolicitacaoAbastecimentoEntity solicitacao
            where (:agenciaId is null or solicitacao.agencia.id = :agenciaId)
              and (:status is null or solicitacao.status = :status)
              and (:inicio is null or solicitacao.dataCriacao >= :inicio)
              and (:fimExclusivo is null or solicitacao.dataCriacao < :fimExclusivo)
            """)
    Page<SolicitacaoAbastecimentoEntity> buscar(@Param("agenciaId") Long agenciaId,
            @Param("status") StatusSolicitacao status, @Param("inicio") Instant inicio,
            @Param("fimExclusivo") Instant fimExclusivo, Pageable pageable);
}

