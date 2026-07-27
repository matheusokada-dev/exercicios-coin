package br.com.gestaonumerario.bff.dto;

import java.math.BigDecimal;
import java.util.List;

public record DetalheSolicitacaoNumerarioResponse(
        SolicitacaoNumerarioResponse solicitacao,
        OperacaoResumo operacao,
        List<HistoricoSolicitacaoResponse> historico) {
    public record OperacaoResumo(Long id,String status,Long origemId,Long destinoId,
            BigDecimal valorProgramado,BigDecimal valorExpedido,BigDecimal valorRecebido,
            BigDecimal valorDivergencia,long versao) {}
}
