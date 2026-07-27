package br.com.gestaonumerario.api.adapter.output.repository;

import br.com.gestaonumerario.api.adapter.output.repository.entity.HistoricoSolicitacaoNumerarioEntity;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface HistoricoSolicitacaoNumerarioJpaRepository
        extends JpaRepository<HistoricoSolicitacaoNumerarioEntity, Long> {
    @EntityGraph(attributePaths={"usuario","operacao"})
    List<HistoricoSolicitacaoNumerarioEntity> findBySolicitacao_IdOrderByDataEventoAscIdAsc(Long solicitacaoId);
}
