package br.com.gestaonumerario.api.adapter.output.repository;

import br.com.gestaonumerario.api.adapter.output.repository.entity.MovimentacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;

import java.time.Instant;
import java.math.BigDecimal;

public interface MovimentacaoJpaRepository
        extends JpaRepository<MovimentacaoEntity, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    long countByTipoAndDataMovimentoGreaterThanEqualAndDataMovimentoLessThan(
            TipoMovimentacao tipo,
            Instant inicio,
            Instant fimExclusivo
    );

    @Query("""
            select coalesce(sum(movimentacao.valor), 0) from MovimentacaoEntity movimentacao
            where movimentacao.tipo = :tipo
              and movimentacao.dataMovimento >= :inicio
              and movimentacao.dataMovimento < :fimExclusivo
            """)
    BigDecimal somarValorPorTipoEPeriodo(
            @Param("tipo") TipoMovimentacao tipo,
            @Param("inicio") Instant inicio,
            @Param("fimExclusivo") Instant fimExclusivo
    );

    @Query("""
            select coalesce(sum(movimentacao.valor), 0) from MovimentacaoEntity movimentacao
            where movimentacao.agencia.id = :agenciaId
              and movimentacao.entrada = :entrada
              and movimentacao.dataMovimento >= :inicio
              and movimentacao.dataMovimento < :fimExclusivo
            """)
    BigDecimal somarValorPorAgenciaEDirecaoNoPeriodo(
            @Param("agenciaId") Long agenciaId,
            @Param("entrada") boolean entrada,
            @Param("inicio") Instant inicio,
            @Param("fimExclusivo") Instant fimExclusivo
    );

    @EntityGraph(attributePaths = {
            "agencia", "usuario", "solicitacao", "solicitacao.agencia",
            "solicitacao.solicitante", "solicitacao.decisor"
    })
    @Query("""
            select movimentacao from MovimentacaoEntity movimentacao
            where (:agenciaId is null or movimentacao.agencia.id = :agenciaId)
              and (:tipo is null or movimentacao.tipo = :tipo)
              and (:inicio is null or movimentacao.dataMovimento >= :inicio)
              and (:fimExclusivo is null or movimentacao.dataMovimento < :fimExclusivo)
            """)
    Page<MovimentacaoEntity> buscar(
            @Param("agenciaId") Long agenciaId,
            @Param("tipo") TipoMovimentacao tipo,
            @Param("inicio") Instant inicio,
            @Param("fimExclusivo") Instant fimExclusivo,
            Pageable pageable
    );
}

