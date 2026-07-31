package br.com.gestaonumerario.api.adapter.input.controller.dto.response;

import br.com.gestaonumerario.api.core.domain.enums.EventoHistoricoSolicitacao;
import br.com.gestaonumerario.api.core.domain.enums.StatusOperacaoNumerario;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record DetalheSolicitacaoNumerarioResponse(
        SolicitacaoNumerarioResponse solicitacao,
        OperacaoResumo operacao,
        List<HistoricoResumo> historico
) {
    public record OperacaoResumo(
            Long id,
            StatusOperacaoNumerario status,
            Long origemId,
            Long destinoId,
            BigDecimal valorProgramado,
            BigDecimal valorExpedido,
            BigDecimal valorRecebido,
            BigDecimal valorDivergencia,
            long versao
    ) {
    }
    public record HistoricoResumo(
            Long id,
            EventoHistoricoSolicitacao evento,
            String statusAnterior,
            String statusNovo,
            Long usuarioId,
            Instant dataEvento,
            String justificativa,
            Map<String, Object> dados
    ) {
    }
}
