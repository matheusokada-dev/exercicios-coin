package br.com.gestaonumerario.api.adapter.output.repository;

import br.com.gestaonumerario.api.adapter.output.repository.entity.SolicitacaoAbastecimentoEntity;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolicitacaoAbastecimentoJpaRepository extends JpaRepository<SolicitacaoAbastecimentoEntity, Long> {

    boolean existsByAgencia_IdAndStatusIn(Long agenciaId, Collection<StatusSolicitacaoNumerario> status);

    long countByStatus(StatusSolicitacaoNumerario status);

    @EntityGraph(
            attributePaths = {
                    "agencia",
                    "origem",
                    "destino",
                    "solicitante",
                    "decisor"}
    )
    Optional<SolicitacaoAbastecimentoEntity> findByAgencia_IdAndStatus(
            Long agenciaId,
            StatusSolicitacaoNumerario status);

    @EntityGraph(
            attributePaths = {
                    "agencia",
                    "origem",
                    "destino",
                    "solicitante",
                    "decisor"}
    )
    @Query("""
            select solicitacao from SolicitacaoAbastecimentoEntity solicitacao
            where (:agenciaId is null or solicitacao.agencia.id = :agenciaId)
              and (:status is null or solicitacao.status = :status)
              and (:inicio is null or solicitacao.dataCriacao >= :inicio)
              and (:fimExclusivo is null or solicitacao.dataCriacao < :fimExclusivo)
            """)
    Page<SolicitacaoAbastecimentoEntity> buscar(
            @Param("agenciaId") Long agenciaId,
            @Param("status") StatusSolicitacaoNumerario status,
            @Param("inicio") Instant inicio,
            @Param("fimExclusivo") Instant fimExclusivo,
            Pageable pageable);

    @EntityGraph(
            attributePaths = {
                    "agencia",
                    "origem",
                    "destino",
                    "solicitante",
                    "decisor",
                    "canceladoPor"}
    )
    @Query("""
            select s from SolicitacaoAbastecimentoEntity s
            where (:agenciaId is null or s.agencia.id=:agenciaId)
              and (:tipo is null or s.tipoOperacao=:tipo)
              and (:status is null or s.status=:status)
              and (:origemId is null or s.origem.id=:origemId)
              and (:destinoId is null or s.destino.id=:destinoId)
              and (:inicio is null or s.dataCriacao>=:inicio)
              and (:fim is null or s.dataCriacao<:fim)
            """)
    Page<SolicitacaoAbastecimentoEntity> buscarComFiltros(
            @Param("agenciaId") Long agenciaId,
            @Param("tipo") br.com.gestaonumerario.api.core.domain.enums.TipoOperacaoNumerario tipo,
            @Param("status") StatusSolicitacaoNumerario status,
            @Param("origemId") Long origemId,
            @Param("destinoId") Long destinoId,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim,
            Pageable pageable);
}
