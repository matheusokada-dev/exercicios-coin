package br.com.gestaonumerario.api.adapter.output.persistence;

import br.com.gestaonumerario.api.adapter.output.repository.AgenciaJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.MovimentacaoJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.SolicitacaoAbastecimentoJpaRepository;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacao;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.ResumoDashboard;
import br.com.gestaonumerario.api.port.output.DashboardOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class DashboardPersistenceAdapter implements DashboardOutputPort {

    private final AgenciaJpaRepository agenciaRepository;
    private final SolicitacaoAbastecimentoJpaRepository solicitacaoRepository;
    private final MovimentacaoJpaRepository movimentacaoRepository;

    @Override
    public ResumoDashboard consultar(LocalDate dataReferencia) {
        var inicio = dataReferencia.atStartOfDay(ZoneOffset.UTC).toInstant();
        var fimExclusivo = dataReferencia.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        return new ResumoDashboard(
                dataReferencia,
                agenciaRepository.somarSaldoDasAgenciasAtivas(),
                agenciaRepository.contarAgenciasAtivasEmAlerta(),
                solicitacaoRepository.countByStatus(StatusSolicitacaoNumerario.PENDENTE),
                movimentacaoRepository.countByTipoAndDataMovimentoGreaterThanEqualAndDataMovimentoLessThan(
                        TipoMovimentacao.ABASTECIMENTO, inicio, fimExclusivo),
                movimentacaoRepository.somarValorPorTipoEPeriodo(TipoMovimentacao.ABASTECIMENTO, inicio, fimExclusivo)
        );
    }
}

