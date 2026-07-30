package br.com.gestaonumerario.bff.dto;

import java.time.Instant;
import java.util.Map;

public record HistoricoSolicitacaoResponse(
        Long id,
        String evento,
        String statusAnterior,
        String statusNovo,
        Long usuarioId,
        Instant dataEvento,
        String justificativa,
        Map<String, Object> dados
) {
}
