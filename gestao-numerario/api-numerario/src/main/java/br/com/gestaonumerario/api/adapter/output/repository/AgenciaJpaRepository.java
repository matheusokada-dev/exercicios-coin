package br.com.gestaonumerario.api.adapter.output.repository;

import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AgenciaJpaRepository extends JpaRepository<AgenciaEntity, Long> {

    boolean existsByCodigo(String codigo);

    @Override
    @EntityGraph(attributePaths = "unidadeOperacional")
    Optional<AgenciaEntity> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "unidadeOperacional")
    java.util.List<AgenciaEntity> findAll();

    @Query("select coalesce(sum(agencia.unidadeOperacional.saldoAtual), 0) from AgenciaEntity agencia where agencia.ativo = true")
    BigDecimal somarSaldoDasAgenciasAtivas();

    @Query("select count(agencia) from AgenciaEntity agencia where agencia.ativo = true and agencia.unidadeOperacional.saldoAtual < agencia.limiteMinimo")
    long contarAgenciasAtivasEmAlerta();

    @EntityGraph(attributePaths = "unidadeOperacional")
    @Query("""
            select agencia from AgenciaEntity agencia
            where (:busca is null
                    or lower(agencia.codigo) like lower(concat('%', :busca, '%'))
                    or lower(agencia.nome) like lower(concat('%', :busca, '%'))
                    or lower(agencia.cidade) like lower(concat('%', :busca, '%')))
              and (:ativo is null or agencia.ativo = :ativo)
              and (:alerta is null
                    or (:alerta = true and agencia.unidadeOperacional.saldoAtual < agencia.limiteMinimo)
                    or (:alerta = false and agencia.unidadeOperacional.saldoAtual >= agencia.limiteMinimo))
            """)
    Page<AgenciaEntity> buscar(
            @Param("busca") String busca,
            @Param("ativo") Boolean ativo,
            @Param("alerta") Boolean alerta,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select agencia from AgenciaEntity agencia join fetch agencia.unidadeOperacional where agencia.id = :id")
    Optional<AgenciaEntity> buscarPorIdParaAtualizacao(@Param("id") Long id);
}
