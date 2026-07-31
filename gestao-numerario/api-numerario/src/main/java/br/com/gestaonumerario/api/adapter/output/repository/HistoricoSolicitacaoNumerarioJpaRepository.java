package br.com.gestaonumerario.api.adapter.output.repository;

import br.com.gestaonumerario.api.adapter.output.repository.entity.HistoricoSolicitacaoNumerarioEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoSolicitacaoNumerarioJpaRepository
        extends
            JpaRepository<HistoricoSolicitacaoNumerarioEntity, Long> {
    @EntityGraph(
            attributePaths = {
                    "usuario",
                    "operacao"}
    )
    List<HistoricoSolicitacaoNumerarioEntity> findBySolicitacao_IdOrderByDataEventoAscIdAsc(Long solicitacaoId);
}
