package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.EventoHistoricoSolicitacao;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record HistoricoSolicitacaoNumerario(
        Long id,
        Long solicitacaoId,
        Long operacaoId,
        EventoHistoricoSolicitacao evento,
        String statusAnterior,
        String statusNovo,
        Long usuarioId,
        Instant dataEvento,
        String justificativa,
        Map<String, Object> dadosComplementares
) {
    public HistoricoSolicitacaoNumerario {
        if (evento == null || statusNovo == null || statusNovo.isBlank() || usuarioId == null || dataEvento == null) {
            throw new CampoObrigatorioException();
        }
        statusAnterior = textoOpcional(statusAnterior);
        statusNovo = statusNovo.trim();
        justificativa = textoOpcional(justificativa);
        dadosComplementares = dadosComplementares == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(dadosComplementares));
    }

    private static String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
