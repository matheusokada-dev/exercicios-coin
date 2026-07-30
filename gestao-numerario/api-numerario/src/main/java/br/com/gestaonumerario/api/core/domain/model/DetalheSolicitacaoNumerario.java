package br.com.gestaonumerario.api.core.domain.model;

import java.util.List;

public record DetalheSolicitacaoNumerario(
        SolicitacaoNumerario solicitacao,
        OperacaoNumerario operacao,
        List<HistoricoSolicitacaoNumerario> historico
) {
    public DetalheSolicitacaoNumerario {
        historico = List.copyOf(historico);
    }
}
